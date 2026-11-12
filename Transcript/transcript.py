#!/usr/bin/env python3
"""
Video transcript generator for YouTube and Instagram.

Run it, paste a video URL, and the transcript is written next to this script:
    YouTube   -> youtube_transcript.txt
    Instagram -> instagram_transcript.txt

Strategy (chosen for accuracy first, then speed):
    YouTube:
      1. Human-made captions in the video's spoken language  (~1-3 s, best accuracy)
      2. No human captions:
           English            -> Whisper large-v3-turbo on the audio (beat YouTube auto-captions)
           other languages    -> YouTube's own auto-captions (beat Whisper on Hindi)
      3. YouTube auto-generated captions as a last resort if Whisper is impossible
    Instagram (no public captions exist):
      Whisper large-v3-turbo on the downloaded audio.

Audio is trimmed to speech with Silero VAD before Whisper runs. That makes
transcription faster and stops Whisper from inventing text during music or
silence (a well-known Whisper failure mode).
"""

from __future__ import annotations

import argparse
import html
import importlib.util
import json
import os
import platform
import re
import shutil
import subprocess
import sys
import tempfile
import threading
import unicodedata
import time
import urllib.parse
import urllib.request
from dataclasses import dataclass, field
from datetime import date
from pathlib import Path
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    import numpy as np

SCRIPT_DIR = Path(__file__).resolve().parent
OUTPUT_FILES = {"youtube": "youtube_transcript.txt", "instagram": "instagram_transcript.txt"}
PLATFORM_NAMES = {"youtube": "YouTube", "instagram": "Instagram"}
COOKIE_BROWSERS = ("brave", "chrome", "chromium", "edge", "firefox", "opera", "safari", "vivaldi", "whale")

SAMPLE_RATE = 16_000
MIN_SPEECH_SECONDS = 1.0
MIN_CAPTION_COVERAGE = 0.5  # human captions must span at least half of the video
CAPTION_METADATA_GRACE_SECONDS = 1.5
BROWSER_UA = (
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"
)

os.environ.setdefault("HF_HUB_DISABLE_TELEMETRY", "1")
os.environ.setdefault("TOKENIZERS_PARALLELISM", "false")


class TranscriptError(Exception):
    """An expected failure: shown to the user as a message plus a hint, without a traceback."""

    def __init__(self, message: str, hint: str | None = None):
        super().__init__(message)
        self.hint = hint


def status(message: str) -> None:
    print(f"  -> {message}", flush=True)


# --------------------------------------------------------------------------------------
# URL detection
# --------------------------------------------------------------------------------------

YT_ID_RE = re.compile(r"^[A-Za-z0-9_-]{11}$")
IG_SHORTCODE_RE = re.compile(r"^[A-Za-z0-9_-]{5,64}$")
YT_PATH_KINDS = {"shorts", "live", "embed", "v", "e"}
IG_MEDIA_KINDS = {"p", "reel", "reels", "tv"}


@dataclass(frozen=True)
class VideoRef:
    platform: str  # "youtube" | "instagram"
    video_id: str
    url: str  # canonical URL handed to the downloaders


def _host_is(host: str, domain: str) -> bool:
    """Exact domain or a real subdomain of it. 'notyoutube.com' and 'youtube.com.evil.io' do not match."""
    return host == domain or host.endswith("." + domain)


def _parse_url(raw: str) -> urllib.parse.SplitResult:
    text = raw.strip().strip("<>\"'`").strip()
    if not text:
        raise TranscriptError("No URL was entered.")
    if any(ch.isspace() for ch in text):
        raise TranscriptError("The input contains spaces. Paste exactly one video URL.")
    if not re.match(r"^[A-Za-z][A-Za-z0-9+.-]*://", text):
        text = "https://" + text  # allow "youtu.be/xyz" without a scheme
    try:
        parts = urllib.parse.urlsplit(text)
        _ = parts.port  # raises ValueError on a malformed port
    except ValueError as exc:
        raise TranscriptError(f"This is not a valid URL: {raw.strip()!r}") from exc
    if parts.scheme.lower() not in ("http", "https") or not parts.hostname:
        raise TranscriptError(f"This is not a valid web URL: {raw.strip()!r}")
    return parts


def detect_platform(raw_url: str, *, resolve_share_links: bool = True) -> VideoRef:
    """Identify the platform and video ID from a URL. Raises TranscriptError for anything else."""
    parts = _parse_url(raw_url)
    host = parts.hostname.rstrip(".").lower()
    segments = [s for s in parts.path.split("/") if s]
    query = urllib.parse.parse_qs(parts.query)

    if host in ("youtu.be", "www.youtu.be"):
        return _youtube_ref(segments[0] if segments else "")

    if _host_is(host, "youtube.com") or _host_is(host, "youtube-nocookie.com"):
        if segments[:1] == ["watch"]:
            return _youtube_ref((query.get("v") or [""])[0])
        if len(segments) >= 2 and segments[0] in YT_PATH_KINDS:
            return _youtube_ref(segments[1])
        raise TranscriptError(
            "This YouTube link does not point to a single video.",
            "Use a video link such as https://www.youtube.com/watch?v=VIDEO_ID or "
            "https://youtube.com/shorts/VIDEO_ID (channel, playlist and search pages are not supported).",
        )

    if _host_is(host, "instagram.com") or _host_is(host, "instagr.am"):
        if segments[:1] == ["share"] and resolve_share_links:
            return detect_platform(_resolve_instagram_share_link(parts.geturl()), resolve_share_links=False)
        if len(segments) >= 2 and segments[0] in IG_MEDIA_KINDS:
            return _instagram_ref(segments[0], segments[1])
        if len(segments) >= 3 and segments[1] in IG_MEDIA_KINDS:  # /<username>/reel/<code>/
            return _instagram_ref(segments[1], segments[2])
        if len(segments) >= 3 and segments[0] == "stories" and segments[2].isdigit():
            story_url = f"https://www.instagram.com/stories/{segments[1]}/{segments[2]}/"
            return VideoRef("instagram", segments[2], story_url)
        raise TranscriptError(
            "This Instagram link does not point to a reel or video post.",
            "Use a link such as https://www.instagram.com/reel/SHORTCODE/ or "
            "https://www.instagram.com/p/SHORTCODE/ (profile pages are not supported).",
        )

    raise TranscriptError(
        f"Unsupported website: {host}",
        "Only YouTube and Instagram video links are supported.",
    )


def _youtube_ref(video_id: str) -> VideoRef:
    if not YT_ID_RE.match(video_id):
        raise TranscriptError(
            f"Invalid YouTube video ID: {video_id!r}",
            "A YouTube video ID is exactly 11 characters. Copy the link again from the Share button.",
        )
    return VideoRef("youtube", video_id, f"https://www.youtube.com/watch?v={video_id}")


def _instagram_ref(kind: str, shortcode: str) -> VideoRef:
    if not IG_SHORTCODE_RE.match(shortcode):
        raise TranscriptError(f"Invalid Instagram post code: {shortcode!r}")
    kind = "reel" if kind == "reels" else kind
    return VideoRef("instagram", shortcode, f"https://www.instagram.com/{kind}/{shortcode}/")


def _resolve_instagram_share_link(url: str) -> str:
    """instagram.com/share/... links redirect to the real post; follow them once."""
    status("Resolving Instagram share link")
    request = urllib.request.Request(url, headers={"User-Agent": BROWSER_UA})
    try:
        with urllib.request.urlopen(request, timeout=15) as response:
            final = response.geturl()
    except Exception as exc:  # noqa: BLE001 - any network error means "cannot resolve"
        raise TranscriptError(
            "Could not open the Instagram share link.",
            "Open the link in a browser and copy the full URL (it looks like instagram.com/reel/...).",
        ) from exc
    parts = urllib.parse.urlsplit(final)
    if parts.path.startswith("/accounts/login"):  # login wall: the target is in ?next=
        nxt = urllib.parse.parse_qs(parts.query).get("next", [""])[0]
        if nxt.startswith("/"):
            final = "https://www.instagram.com" + nxt
    if "/share/" in urllib.parse.urlsplit(final).path:
        raise TranscriptError(
            "Instagram did not reveal which post this share link points to.",
            "Open the link in a browser and copy the full URL (it looks like instagram.com/reel/...).",
        )
    return final


# --------------------------------------------------------------------------------------
# Transcript model and text cleanup
# --------------------------------------------------------------------------------------

@dataclass
class Segment:
    start: float
    end: float
    text: str


@dataclass
class Transcript:
    segments: list[Segment]
    method: str
    language: str | None = None
    duration: float | None = None
    notes: list[str] = field(default_factory=list)

    @property
    def word_count(self) -> int:
        return sum(len(s.text.split()) for s in self.segments)


NON_SPEECH_RE = re.compile(
    r"\[[^\]]*\]"  # [Music], [Applause], [♪♪♪]
    r"|[♪♫]+"
    r"|\((?:laughter|laughs|applause|music|cheering|cheers|inaudible|silence|sighs?|chuckles?)\)",
    re.IGNORECASE,
)
_REPEAT_RE = re.compile(r"(?<!\S)((?:\S+\s+){0,3}?\S+)(?:\s+\1){3,}(?!\S)")


def clean_text(text: str) -> str:
    text = html.unescape(text).replace("\n", " ")
    text = NON_SPEECH_RE.sub(" ", text)
    text = re.sub(r"^\s*>>\s*", "", text)  # caption speaker-change marker
    return re.sub(r"\s+", " ", text).strip()


def collapse_repeats(text: str) -> str:
    """'a b a b a b a b c' -> 'a b c': a phrase of 1-4 words repeated 4+ times in a row is a decoding loop."""
    return _REPEAT_RE.sub(r"\1", text)


def tidy_segments(segments: list[Segment]) -> list[Segment]:
    """Clean text, drop empty pieces, and collapse repetition loops (same line 3+ times in a row)."""
    result: list[Segment] = []
    repeat = 0
    for seg in segments:
        text = collapse_repeats(clean_text(seg.text))
        if not text:
            continue
        if result and text.casefold() == result[-1].text.casefold():
            repeat += 1
            if repeat >= 2:
                result[-1].end = seg.end
                continue
        else:
            repeat = 0
        result.append(Segment(seg.start, seg.end, text))
    return result


def format_timestamp(seconds: float) -> str:
    seconds = int(max(seconds, 0))
    h, rem = divmod(seconds, 3600)
    m, s = divmod(rem, 60)
    return f"{h:02d}:{m:02d}:{s:02d}" if h else f"{m:02d}:{s:02d}"


def build_paragraphs(segments: list[Segment]) -> list[str]:
    """Group segments into readable paragraphs: break on long pauses or at sentence ends once long enough."""
    paragraphs: list[str] = []
    current: list[str] = []
    words = 0
    prev_end: float | None = None
    for seg in segments:
        pause = seg.start - prev_end if prev_end is not None else 0.0
        ends_sentence = bool(current) and current[-1].rstrip().endswith((".", "?", "!", "।", "。", "؟"))
        if current and ((pause >= 2.0 and words >= 30) or (words >= 120 and ends_sentence)):
            paragraphs.append(" ".join(current))
            current, words = [], 0
        current.append(seg.text)
        words += len(seg.text.split())
        prev_end = seg.end
    if current:
        paragraphs.append(" ".join(current))
    return paragraphs


# Line wrapping ---------------------------------------------------------------------------
# Width is measured in terminal/editor columns, not characters: in Hindi ~40% of code points are
# vowel signs and viramas that share a column with their consonant (len("इंटरव्यू") == 8, but it
# occupies 5 columns), so len()-based wrapping (e.g. textwrap) would make Devanagari lines look
# far shorter than English ones. wcwidth is the library terminals use for exactly this.
DEFAULT_LINE_WIDTH = 80
_CLOSING_PUNCTUATION = set(".,;:!?)]}\"'।॥…–—")


def display_width(text: str) -> int:
    """Columns `text` occupies in a terminal / monospace editor."""
    try:
        from wcwidth import wcswidth

        width = wcswidth(text)
        if width >= 0:
            return width
    except ImportError:
        pass
    # Fallback without wcwidth: marks that combine with the previous letter take no column, wide CJK takes two.
    width = 0
    for ch in text:
        if unicodedata.category(ch) in ("Mn", "Me", "Cf"):
            continue
        width += 2 if unicodedata.east_asian_width(ch) in ("W", "F") else 1
    return width


def wrap_text(text: str, width: int, first_prefix: str = "", indent: str = "") -> list[str]:
    """Greedy word wrap by display width. Words are never split; a word longer than the line gets its own line.

    `first_prefix` starts the first line (e.g. "[01:05] ") and `indent` aligns the following lines under the text.
    """
    tokens: list[str] = []
    for word in text.split():
        if tokens and all(ch in _CLOSING_PUNCTUATION for ch in word):
            tokens[-1] += word  # never start a line with a lone "।" or "?"
        else:
            tokens.append(word)
    if width <= 0 or not tokens:
        return [first_prefix + " ".join(tokens)]

    lines: list[str] = []
    line, line_width = first_prefix, display_width(first_prefix)
    line_has_word = False
    for token in tokens:
        token_width = display_width(token)
        if line_has_word and line_width + 1 + token_width > width:
            lines.append(line)
            line, line_width, line_has_word = indent, display_width(indent), False
        if line_has_word:
            line, line_width = line + " " + token, line_width + 1 + token_width
        else:
            line, line_width = line + token, line_width + token_width
        line_has_word = True
    lines.append(line)
    return lines


def render_transcript(t: Transcript, with_timestamps: bool, width: int = DEFAULT_LINE_WIDTH) -> str:
    """Only the spoken text goes into the file, wrapped to `width` columns (0 = one line per paragraph)."""
    if with_timestamps:
        lines: list[str] = []
        for seg in t.segments:
            prefix = f"[{format_timestamp(seg.start)}] "
            lines.extend(wrap_text(seg.text, width, prefix, " " * len(prefix)))
        body = "\n".join(lines)
    else:
        body = "\n\n".join("\n".join(wrap_text(p, width)) for p in build_paragraphs(t.segments))
    return body + "\n"


def write_atomically(path: Path, content: str) -> None:
    """Write via a temp file + rename so an interrupted run never leaves a half-written transcript."""
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, tmp = tempfile.mkstemp(prefix=".transcript-", suffix=".tmp", dir=path.parent)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as fh:
            fh.write(content)
        umask = os.umask(0)
        os.umask(umask)
        os.chmod(tmp, 0o666 & ~umask)  # mkstemp creates 0600; give the file normal permissions
        os.replace(tmp, path)
    except BaseException:
        Path(tmp).unlink(missing_ok=True)
        raise


# --------------------------------------------------------------------------------------
# Downloading (yt-dlp)
# --------------------------------------------------------------------------------------

class _YtdlpLogger:
    """Keeps yt-dlp quiet but surfaces the one message users care about (site-imposed waits)."""

    def __init__(self) -> None:
        self.warnings: list[str] = []

    def debug(self, msg: str) -> None:
        match = re.search(r"Sleeping ([\d.]+) seconds as required by the site", msg)
        if match:
            status(f"Site asks for a {float(match.group(1)):.0f}s wait before the download starts (normal)")

    def info(self, msg: str) -> None:
        self.debug(msg)

    def warning(self, msg: str) -> None:
        self.warnings.append(msg)

    def error(self, msg: str) -> None:
        self.warnings.append(msg)


def _progress_hook(d: dict) -> None:
    if not sys.stdout.isatty():
        return
    if d.get("status") == "downloading":
        total = d.get("total_bytes") or d.get("total_bytes_estimate")
        done = d.get("downloaded_bytes") or 0
        if total:
            print(f"\r  -> Downloading audio: {done / total:6.1%} of {total / 1_048_576:.1f} MiB", end="", flush=True)
    elif d.get("status") == "finished":
        print("\r  -> Downloading audio: done" + " " * 30, flush=True)


def available_js_runtimes() -> dict:
    """yt-dlp needs a JavaScript runtime (deno or node>=20) to read YouTube's player."""
    runtimes = {name: {} for name in ("deno", "node", "bun") if shutil.which(name)}
    return runtimes or {"deno": {}}


def make_ydl_options(workdir: Path, args: argparse.Namespace) -> dict:
    opts = {
        "quiet": True,
        "no_warnings": False,
        "noprogress": True,
        "noplaylist": True,
        "logger": _YtdlpLogger(),
        "progress_hooks": [_progress_hook],
        # Audio only, closest to 64 kbit/s: plenty for speech recognition and the smallest download.
        "format": "bestaudio/best",
        "format_sort": ["abr~64"],
        "outtmpl": {"default": str(workdir / "%(id)s.%(ext)s")},
        "paths": {"home": str(workdir), "temp": str(workdir)},
        "js_runtimes": available_js_runtimes(),
        "retries": 5,
        "fragment_retries": 5,
        "extractor_retries": 3,
        "socket_timeout": 30,
        "http_chunk_size": 10 * 1024 * 1024,
        "concurrent_fragment_downloads": 4,
    }
    if args.cookies:
        opts["cookiefile"] = str(Path(args.cookies).expanduser())
    if args.cookies_from_browser:
        opts["cookiesfrombrowser"] = (args.cookies_from_browser.lower(), None, None, None)
    return opts


_YTDLP_ERROR_HINTS: list[tuple[str, str, str | None]] = [
    # (regex on yt-dlp's message, message for the user, hint)
    (r"private video|this video is private", "This YouTube video is private.",
     "If your account can watch it, run with --cookies-from-browser chrome (or safari/firefox)."),
    (r"sign in to confirm your age|age.restricted|inappropriate for some users",
     "This video is age-restricted.", "Run with --cookies-from-browser chrome (logged-in, age-verified account)."),
    (r"not a bot", "YouTube is asking to confirm you are not a bot (rate limit on your IP).",
     "Wait a while, update yt-dlp (pip install -U 'yt-dlp[default,curl-cffi]'), or use --cookies-from-browser chrome."),
    (r"members.only|join this channel", "This is a members-only video.",
     "Run with --cookies-from-browser chrome using an account that is a channel member."),
    (r"premieres in|live event will begin|this live event", "This video has not started yet (upcoming premiere/live).", None),
    (r"unavailable|not available|has been removed|account associated with this video has been terminated|does not exist",
     "The video is unavailable (deleted, region-blocked, or the link is wrong).", None),
    (r"empty media response|login required|unreachable|requested content is not available|not granting access|private",
     "Instagram did not return this post without logging in (private account, deleted post, or login wall).",
     "If you can see it in your browser while logged in, run: python transcript.py --cookies-from-browser chrome"),
    (r"no video formats|there is no video in this post|no video",
     "This post does not contain a video (it may be a photo post).", None),
    (r"http error 429|too many requests|rate.limit", "The site is rate-limiting requests from your network.",
     "Wait a few minutes and try again, or pass --cookies-from-browser chrome."),
    (r"getaddrinfo|name or service not known|timed out|connection (reset|refused)|network is unreachable",
     "Network error while contacting the site.", "Check your internet connection and try again."),
    (r"requested format is not available|signature|nsig|player", "yt-dlp could not read the video's media formats.",
     "Update yt-dlp: pip install -U 'yt-dlp[default,curl-cffi]'. For YouTube also make sure deno or node is installed."),
]


def explain_download_error(exc: Exception, platform_key: str) -> TranscriptError:
    raw = re.sub(r"\x1b\[[0-9;]*m", "", str(exc))
    raw = re.sub(r"^ERROR:\s*", "", raw).strip()
    lowered = raw.lower()
    for pattern, message, hint in _YTDLP_ERROR_HINTS:
        if "Instagram" in message and platform_key != "instagram":
            continue
        if "YouTube" in message and platform_key != "youtube":
            continue
        if re.search(pattern, lowered):
            return TranscriptError(f"{message}\n     Details: {raw[:300]}", hint)
    return TranscriptError(
        f"Download failed: {raw[:400]}",
        "Update yt-dlp first (pip install -U 'yt-dlp[default,curl-cffi]'); sites change often.",
    )


def check_live_status(info: dict) -> None:
    live_status = info.get("live_status")
    if live_status == "is_live" or info.get("is_live"):
        raise TranscriptError("This is a live stream that is still running.", "Try again after the stream has ended.")
    if live_status == "is_upcoming":
        raise TranscriptError("This video is an upcoming premiere/live stream and has no content yet.")


def download_audio_files(ydl, info: dict) -> list[tuple[dict, Path]]:
    """Download the selected audio format(s) for an already-extracted info dict (no second extraction)."""
    result = ydl.process_ie_result(info, download=True)
    entries = [e for e in (result.get("entries") or [result]) if e]
    files: list[tuple[dict, Path]] = []
    for entry in entries:
        for item in entry.get("requested_downloads") or []:
            path = Path(item.get("filepath") or "")
            if path.is_file() and path.stat().st_size > 0:
                files.append((entry, path))
                break
    if not files:
        raise TranscriptError("No audio could be downloaded from this video.")
    return files


# --------------------------------------------------------------------------------------
# Speech recognition (Whisper) with VAD
# --------------------------------------------------------------------------------------

def load_audio(path: Path) -> np.ndarray:
    import numpy as np

    if not shutil.which("ffmpeg"):
        raise TranscriptError("ffmpeg is not installed.", "Install it with: brew install ffmpeg")
    cmd = ["ffmpeg", "-nostdin", "-loglevel", "error", "-i", str(path),
           "-f", "s16le", "-ac", "1", "-ar", str(SAMPLE_RATE), "-"]
    proc = subprocess.run(cmd, capture_output=True)
    if proc.returncode != 0 or not proc.stdout:
        raise TranscriptError(f"ffmpeg could not decode the audio: {proc.stderr.decode(errors='replace')[:300]}")
    return np.frombuffer(proc.stdout, np.int16).astype(np.float32) / 32768.0


@dataclass
class SpeechMap:
    """Maps a time in the speech-only audio back to the original video time."""

    chunks: list[tuple[float, float, float]]  # (start in concatenated audio, original start, length)

    def to_original(self, t: float) -> float:
        for concat_start, orig_start, length in reversed(self.chunks):
            if t >= concat_start:
                return orig_start + min(t - concat_start, length)
        return t


def extract_speech(audio: np.ndarray) -> tuple[np.ndarray, SpeechMap, float]:
    """Run Silero VAD and return (speech-only audio, time map, seconds of speech)."""
    import numpy as np
    from faster_whisper.vad import VadOptions, get_speech_timestamps

    stamps = get_speech_timestamps(audio, VadOptions(), sampling_rate=SAMPLE_RATE)
    pieces, chunks, cursor = [], [], 0.0
    for st in stamps:
        piece = audio[st["start"]:st["end"]]
        pieces.append(piece)
        chunks.append((cursor, st["start"] / SAMPLE_RATE, len(piece) / SAMPLE_RATE))
        cursor += len(piece) / SAMPLE_RATE
    speech = np.concatenate(pieces) if pieces else np.zeros(0, dtype=np.float32)
    return speech, SpeechMap(chunks), cursor


# Decoding settings shared by both backends, chosen from measurements (see README):
#  * condition_on_previous_text=False: stops runaway repetition loops on long audio.
#  * max 440 new tokens per 30 s window instead of Whisper's default 224. Scripts such as Devanagari
#    need many tokens per word; at 224 a window is cut mid-word ("�") and the rest of its speech
#    is silently lost (Hindi test: 42% of words missing). 440 + ~4 prompt tokens fits the 448 context.
#  * compression-ratio check off: gzip ratio is inflated for non-Latin UTF-8 text, so the check
#    misfires on normal Hindi and forces random high-temperature re-decoding. Repetition loops are
#    removed by collapse_repeats() instead. English accuracy was unchanged (WER 4.0% both ways).
MAX_NEW_TOKENS = 440
WHISPER_MODELS = {  # --model choice -> (mlx repo, faster-whisper name)
    "turbo": ("mlx-community/whisper-large-v3-turbo", "large-v3-turbo"),
    "large-v3": ("mlx-community/whisper-large-v3-mlx", "large-v3"),
    "medium": ("mlx-community/whisper-medium-mlx", "medium"),
    "small": ("mlx-community/whisper-small-mlx", "small"),
    "base": ("mlx-community/whisper-base-mlx", "base"),
    "tiny": ("mlx-community/whisper-tiny-mlx", "tiny"),
}


class WhisperEngine:
    name = "whisper"

    def __init__(self, model: str = "turbo"):
        self.model = model

    @property
    def model_label(self) -> str:
        return f"Whisper {'large-v3-turbo' if self.model == 'turbo' else self.model}"

    def transcribe(self, audio, language: str | None) -> tuple[list[dict], str | None]:
        raise NotImplementedError


class MlxWhisperEngine(WhisperEngine):
    """Apple Silicon GPU via MLX. Fastest option on M-series Macs."""

    name = "mlx-whisper"

    def transcribe(self, audio, language):
        import mlx_whisper

        result = mlx_whisper.transcribe(
            audio,
            path_or_hf_repo=WHISPER_MODELS[self.model][0],
            language=language,
            condition_on_previous_text=False,
            compression_ratio_threshold=None,
            sample_len=MAX_NEW_TOKENS,
            verbose=None,
        )
        segments = [
            {"start": s["start"], "end": s["end"], "text": s["text"],
             "avg_logprob": s.get("avg_logprob", 0.0), "no_speech_prob": s.get("no_speech_prob", 0.0)}
            for s in result.get("segments", [])
        ]
        return segments, result.get("language")


class FasterWhisperEngine(WhisperEngine):
    """CTranslate2 backend: CUDA if available, otherwise int8 on CPU. Works on any OS."""

    name = "faster-whisper"

    def transcribe(self, audio, language):
        import ctranslate2
        from faster_whisper import WhisperModel

        cuda = ctranslate2.get_cuda_device_count() > 0
        model = WhisperModel(
            WHISPER_MODELS[self.model][1],
            device="cuda" if cuda else "cpu",
            compute_type="float16" if cuda else "int8",
            cpu_threads=os.cpu_count() or 4,
        )
        segs, info = model.transcribe(
            audio,
            language=language,
            beam_size=5,
            vad_filter=False,  # VAD already applied by extract_speech()
            condition_on_previous_text=False,
            compression_ratio_threshold=None,
            max_new_tokens=MAX_NEW_TOKENS,
        )
        segments = [
            {"start": s.start, "end": s.end, "text": s.text,
             "avg_logprob": s.avg_logprob, "no_speech_prob": s.no_speech_prob}
            for s in segs
        ]
        return segments, info.language


def choose_engine(preference: str, model: str = "turbo") -> WhisperEngine:
    if importlib.util.find_spec("faster_whisper") is None:
        raise TranscriptError(
            "faster-whisper is not installed (needed for speech detection and transcription).",
            "Run: pip install -r requirements.txt",
        )
    apple_silicon = sys.platform == "darwin" and platform.machine() == "arm64"
    has_mlx = importlib.util.find_spec("mlx_whisper") is not None
    if preference == "mlx" or (preference == "auto" and apple_silicon and has_mlx):
        if not has_mlx:
            raise TranscriptError("mlx-whisper is not installed.", "Run: pip install mlx-whisper (Apple Silicon only)")
        return MlxWhisperEngine(model)
    return FasterWhisperEngine(model)


def _base_lang(code: str | None) -> str:
    return (code or "").split("-")[0].lower()


def whisper_language(code: str | None) -> str | None:
    """Map 'en-US' -> 'en'; return None (auto-detect) for codes Whisper does not know."""
    if not code:
        return None
    base = _base_lang(code)
    base = {"iw": "he", "jv": "jw"}.get(base, base)  # YouTube/ISO codes that Whisper spells differently
    try:
        from faster_whisper.tokenizer import _LANGUAGE_CODES as known
    except ImportError:
        return base
    return base if base in known else None


def transcribe_audio_file(path: Path, engine: WhisperEngine, language: str | None) -> tuple[list[Segment], str | None, float, list[str]]:
    """Decode -> VAD -> Whisper -> map timestamps back. Returns (segments, language, audio seconds, notes)."""
    language = whisper_language(language)
    audio = load_audio(path)
    total_seconds = len(audio) / SAMPLE_RATE
    speech, speech_map, speech_seconds = extract_speech(audio)
    if speech_seconds < MIN_SPEECH_SECONDS:
        return [], language, total_seconds, []

    status(f"Transcribing {format_timestamp(speech_seconds)} of speech "
           f"(of {format_timestamp(total_seconds)} audio) with {engine.model_label} [{engine.name}]")
    raw_segments, detected = engine.transcribe(speech, language)

    notes: list[str] = []
    segments: list[Segment] = []
    logprobs: list[float] = []
    for s in raw_segments:
        # Whisper's own "this window was not speech" signal: drop it rather than keep invented text.
        if s["no_speech_prob"] > 0.6 and s["avg_logprob"] < -1.0:
            continue
        segments.append(Segment(speech_map.to_original(s["start"]), speech_map.to_original(s["end"]), s["text"]))
        logprobs.append(s["avg_logprob"])
    if logprobs and sum(logprobs) / len(logprobs) < -1.0:
        notes.append("Low recognition confidence (noisy audio, music over speech, or mixed languages). "
                     "Try --language <code> if the language was guessed wrong.")
    return segments, detected, total_seconds, notes


# --------------------------------------------------------------------------------------
# YouTube
# --------------------------------------------------------------------------------------

def list_youtube_captions(video_id: str):
    """Caption track list, or None if unavailable for any reason (never fatal; yt-dlp is the source of truth)."""
    try:
        from youtube_transcript_api import YouTubeTranscriptApi

        return YouTubeTranscriptApi().list(video_id)
    except Exception:  # noqa: BLE001 - captions are an optimisation; any failure just means "use audio"
        return None


def caption_segments(track) -> list[Segment]:
    fetched = track.fetch()
    return tidy_segments([Segment(s.start, s.start + s.duration, s.text) for s in fetched])


def pick_track(tracks, language: str):
    """Track whose language matches exactly, else the same base language (en-US ~ en), else None."""
    same = [t for t in tracks if _base_lang(t.language_code) == _base_lang(language)]
    exact = [t for t in same if t.language_code.lower() == language.lower()]
    return (exact or same or [None])[0]


def pick_human_track(tracks, spoken_language: str | None):
    manual = [t for t in tracks if not t.is_generated]
    if spoken_language:
        return pick_track(manual, spoken_language)
    return manual[0] if len(manual) == 1 else None


def original_audio_language(info: dict) -> str | None:
    """Language of the original audio track: highest language_preference among audio formats."""
    audio = [
        (f.get("language_preference") if f.get("language_preference") is not None else -1, f["language"])
        for f in info.get("formats") or []
        if f.get("language") and f.get("acodec") not in (None, "none")
    ]
    return max(audio, key=lambda item: item[0])[1] if audio else info.get("language")


def captions_cover_video(segments: list[Segment], duration: float | None) -> bool:
    if not segments:
        return False
    if not duration or duration < 60:
        return True
    return (segments[-1].end - segments[0].start) / duration >= MIN_CAPTION_COVERAGE


class BackgroundCall:
    """Run a function in a daemon thread so a slow, optional lookup never delays the program's exit."""

    def __init__(self, fn, *args, **kwargs):
        self._done = threading.Event()
        self._value = None
        self._error: BaseException | None = None
        threading.Thread(target=self._run, args=(fn, args, kwargs), daemon=True).start()

    def _run(self, fn, args, kwargs) -> None:
        try:
            self._value = fn(*args, **kwargs)
        except BaseException as exc:  # noqa: BLE001 - re-raised in result()
            self._error = exc
        finally:
            self._done.set()

    def result(self, timeout: float | None = None):
        if not self._done.wait(timeout):
            raise TimeoutError
        if self._error is not None:
            raise self._error
        return self._value


def transcribe_youtube(ref: VideoRef, args: argparse.Namespace, workdir: Path) -> Transcript:
    import yt_dlp

    status("Checking captions and video details")
    ydl = yt_dlp.YoutubeDL(make_ydl_options(workdir, args))
    # Full metadata via yt-dlp is slow (several requests + JS challenge) and only strictly needed when
    # we must download audio, so it runs in the background while the fast caption lookup happens.
    meta = BackgroundCall(ydl.extract_info, ref.url, download=False, process=False)
    tracks = list_youtube_captions(ref.video_id)

    def metadata(timeout: float | None) -> tuple[dict | None, TranscriptError | None]:
        try:
            return meta.result(timeout), None
        except TimeoutError:
            return None, None
        except Exception as exc:  # noqa: BLE001
            return None, explain_download_error(exc, "youtube")

    if tracks is None:  # captions unreachable: yt-dlp must work, and its error is the precise one
        info, info_error = metadata(None)
        if info is None:
            raise info_error
    else:
        info, info_error = metadata(0)

    tracks = list(tracks) if tracks is not None else []
    generated = [t for t in tracks if t.is_generated]
    if args.language:
        spoken = args.language
    elif len(generated) == 1:  # the usual case: one speech-recognition track = the spoken language
        spoken = generated[0].language_code
    else:
        # No ASR track, or several (videos with AI-dubbed audio tracks get one per language):
        # only yt-dlp's format list says which audio track is the original.
        if info is None:
            info, info_error = metadata(None)
        spoken = original_audio_language(info) if info else None
    asr_track = pick_track(generated, spoken) if spoken else (generated[0] if len(generated) == 1 else None)

    def from_track(track, method: str, notes: list[str] | None = None) -> Transcript | None:
        nonlocal info
        try:
            segments = caption_segments(track)
        except Exception:  # noqa: BLE001
            return None
        if info is None:  # short grace period for yt-dlp to supply the duration used by the coverage check
            info, _ = metadata(CAPTION_METADATA_GRACE_SECONDS)
        if info:
            check_live_status(info)
        duration = (info or {}).get("duration")
        if not captions_cover_video(segments, duration):
            status("Captions cover only part of the video; ignoring them")
            return None
        return Transcript(segments, method, track.language_code, duration, notes or [])

    # 1) Human-made captions in the spoken language: fastest and most accurate.
    human = pick_human_track(tracks, spoken)
    if human:
        status(f"Found human-made captions ({human.language_code})")
        result = from_track(human, f"YouTube captions (human-made, {human.language_code})")
        if result:
            return result

    auto_method = f"YouTube auto-generated captions ({asr_track.language_code})" if asr_track else ""
    # Measured on test videos: for English, Whisper beat YouTube's auto captions (WER 4.0% vs 6.2%);
    # for Hindi, YouTube's own recognizer was clearly better (correct spelling, punctuation).
    # So non-English videos use YouTube's recognizer unless --whisper is given.
    if asr_track and (args.fast or (_base_lang(asr_track.language_code) != "en" and not args.whisper)):
        reason = "--fast" if args.fast else f"best option for '{asr_track.language_code}' audio"
        status(f"Using YouTube auto-generated captions ({reason})")
        note = ("Auto-generated captions (--fast); Whisper is more accurate for English."
                if args.fast else "YouTube's speech recognition; run with --whisper to use Whisper instead.")
        result = from_track(asr_track, auto_method, [note])
        if result:
            return result

    # 2) Whisper on the audio.
    try:
        engine = choose_engine(args.engine, args.model)
        status("No usable human-made captions; transcribing the audio for best accuracy")
        if info is None:
            info, info_error = metadata(None)
            if info is None:
                raise info_error
        check_live_status(info)
        spoken = spoken or original_audio_language(info)
        (_entry, audio_path), *_ = download_audio_files(ydl, info)
        # Only a language the user typed is forced. Metadata can be wrong (a Dutch vlog tagged "en" was
        # silently *translated* to English when "en" was forced), so Whisper detects it from the audio.
        segments, detected, seconds, notes = transcribe_audio_file(audio_path, engine, args.language)
        if not segments:
            raise TranscriptError("No speech was detected in this video (music-only or silent).")
        if spoken and detected and not args.language and _base_lang(spoken) != _base_lang(detected):
            notes.append(f"YouTube lists the language as '{spoken}', but the audio is '{detected}'; "
                         "the transcript follows the audio.")
        return Transcript(tidy_segments(segments), f"Speech recognition ({engine.model_label}, {engine.name})",
                          detected or spoken, info.get("duration") or seconds, notes)
    except TranscriptError as exc:
        whisper_error = exc
    except yt_dlp.utils.DownloadError as exc:
        whisper_error = explain_download_error(exc, "youtube")
    except Exception as exc:  # noqa: BLE001 - keep the caption fallback available on unexpected engine errors
        whisper_error = TranscriptError(f"Speech recognition failed: {type(exc).__name__}: {exc}")

    # 3) Last resort: YouTube's own automatic captions.
    if asr_track and "No speech was detected" not in str(whisper_error) and "live stream" not in str(whisper_error):
        status(f"Audio transcription failed ({str(whisper_error).splitlines()[0]}); "
               "falling back to auto-generated captions")
        result = from_track(asr_track, auto_method, ["Used because audio transcription failed; may be less accurate."])
        if result:
            return result
    raise whisper_error


# --------------------------------------------------------------------------------------
# Instagram
# --------------------------------------------------------------------------------------

def transcribe_instagram(ref: VideoRef, args: argparse.Namespace, workdir: Path) -> Transcript:
    import yt_dlp

    engine = choose_engine(args.engine, args.model)  # fail fast before any network work
    status("Fetching Instagram post")
    with yt_dlp.YoutubeDL(make_ydl_options(workdir, args)) as ydl:
        try:
            info = ydl.extract_info(ref.url, download=False, process=False)
            check_live_status(info)
            files = download_audio_files(ydl, info)
        except yt_dlp.utils.DownloadError as exc:
            raise explain_download_error(exc, "instagram") from exc

    all_segments: list[Segment] = []
    notes: list[str] = []
    language = None
    offset = 0.0
    for index, (_entry, path) in enumerate(files, start=1):
        if len(files) > 1:
            status(f"Video {index} of {len(files)} in this post")
        segments, detected, seconds, seg_notes = transcribe_audio_file(path, engine, args.language)
        segments = tidy_segments(segments)
        if len(files) > 1 and segments:
            all_segments.append(Segment(offset, offset, f"[Video {index}]"))  # added after cleanup on purpose
        all_segments.extend(Segment(s.start + offset, s.end + offset, s.text) for s in segments)
        language = language or detected
        notes.extend(n for n in seg_notes if n not in notes)
        offset += seconds

    if not all_segments:
        raise TranscriptError("No speech was detected in this Instagram video (music-only or silent).")
    return Transcript(all_segments, f"Speech recognition ({engine.model_label}, {engine.name})",
                      language or args.language, offset, notes)


# --------------------------------------------------------------------------------------
# CLI
# --------------------------------------------------------------------------------------

def yt_dlp_age_days() -> int | None:
    try:
        from yt_dlp.version import __version__

        y, m, d = (int(x) for x in __version__.split(".")[:3])
        return (date.today() - date(y, m, d)).days
    except Exception:  # noqa: BLE001
        return None


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate a transcript from a YouTube or Instagram video URL.",
        epilog="Without a URL argument the script asks for one interactively.",
    )
    parser.add_argument("url", nargs="?", help="video URL (optional; you will be prompted if omitted)")
    parser.add_argument("--language", help="spoken language code, e.g. en, hi (default: auto-detect)")
    parser.add_argument("--timestamps", action="store_true", help="write one [mm:ss] line per segment")
    parser.add_argument("--width", type=int, default=DEFAULT_LINE_WIDTH, metavar="COLUMNS",
                        help=f"wrap lines at this many columns (default: {DEFAULT_LINE_WIDTH}; 0 = no wrapping)")
    parser.add_argument("--fast", action="store_true",
                        help="YouTube: accept auto-generated captions instead of running Whisper (faster, less accurate)")
    parser.add_argument("--whisper", action="store_true",
                        help="YouTube: always run Whisper when no human captions exist, even for non-English videos")
    parser.add_argument("--model", choices=tuple(WHISPER_MODELS), default="turbo",
                        help="Whisper model (default: turbo = large-v3-turbo; large-v3 is slower and larger)")
    parser.add_argument("--engine", choices=("auto", "mlx", "faster-whisper"), default="auto",
                        help="speech recognition backend (default: mlx on Apple Silicon, else faster-whisper)")
    parser.add_argument("--cookies-from-browser", metavar="BROWSER", type=str.lower, choices=COOKIE_BROWSERS,
                        help=f"use your logged-in browser session: {', '.join(COOKIE_BROWSERS)}")
    parser.add_argument("--cookies", metavar="FILE", help="Netscape-format cookies.txt file")
    parser.add_argument("--output-dir", type=Path, default=SCRIPT_DIR,
                        help="folder for the transcript file (default: the script's folder)")
    parser.add_argument("--debug", action="store_true", help="show full tracebacks for unexpected errors")
    args = parser.parse_args(argv)
    if args.language:
        args.language = args.language.strip().lower()
    if args.width < 0 or 0 < args.width < 20:
        parser.error("--width must be 0 (no wrapping) or at least 20")
    return args


def prompt_for_url() -> VideoRef:
    while True:
        try:
            raw = input("Enter YouTube or Instagram video URL: ")
        except EOFError:
            raise TranscriptError("No URL was entered.") from None
        try:
            return detect_platform(raw)
        except TranscriptError as exc:
            print(f"  x {exc}")
            if exc.hint:
                print(f"    {exc.hint}")
            print("    Please try again (Ctrl+C to quit).\n")


def run(args: argparse.Namespace) -> Path:
    ref = detect_platform(args.url) if args.url else prompt_for_url()
    print(f"\nDetected platform: {PLATFORM_NAMES[ref.platform]} (id: {ref.video_id})")
    started = time.monotonic()

    with tempfile.TemporaryDirectory(prefix="transcript-") as tmp:
        handler = transcribe_youtube if ref.platform == "youtube" else transcribe_instagram
        transcript = handler(ref, args, Path(tmp))

    if transcript.word_count == 0:
        raise TranscriptError("The transcript came out empty, so no file was written.")

    output = (args.output_dir.expanduser() / OUTPUT_FILES[ref.platform]).resolve()
    write_atomically(output, render_transcript(transcript, args.timestamps, args.width))

    elapsed = time.monotonic() - started
    print(f"\nSuccess! Transcript generated in {elapsed:.1f}s")
    print(f"  Source   : {transcript.method}")
    print(f"  Language : {transcript.language or 'unknown'}")
    print(f"  Words    : {transcript.word_count}")
    for note in transcript.notes:
        print(f"  Note     : {note}")
    print(f"  Saved    : {output}")
    return output


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    try:
        run(args)
        return 0
    except TranscriptError as exc:
        print(f"\nError: {exc}", file=sys.stderr)
        if exc.hint:
            print(f"Hint : {exc.hint}", file=sys.stderr)
        age = yt_dlp_age_days()
        if age is not None and age > 60:
            print(f"Tip  : your yt-dlp is {age} days old; sites change often. "
                  "Run: pip install -U 'yt-dlp[default,curl-cffi]'", file=sys.stderr)
        return 1
    except KeyboardInterrupt:
        print("\nCancelled.", file=sys.stderr)
        return 130
    except Exception as exc:  # noqa: BLE001 - last-resort guard so users see a message, not a stack dump
        if args.debug:
            raise
        print(f"\nUnexpected error: {type(exc).__name__}: {exc}", file=sys.stderr)
        print("Hint : run again with --debug to see the full traceback.", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
