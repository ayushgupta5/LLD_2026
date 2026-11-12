# Video Transcript Generator (YouTube + Instagram)

`transcript.py` asks for a video URL, works out the platform on its own, and writes **only the
transcript text** to a file next to the script:

| Platform  | Output file              |
|-----------|--------------------------|
| YouTube   | `youtube_transcript.txt`   |
| Instagram | `instagram_transcript.txt` |

Each run overwrites that file. The write is atomic (temp file + rename), so a cancelled run never
leaves a half-written file. Details such as how the transcript was made, its language, word count
and any warnings are printed in the terminal only.

---

## 1. Setup (one time)

```bash
cd /Users/ayushgupta/IdeaProjects/LLD_2026/Transcript
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```

System tools (both are already installed on this Mac):

| Tool | Why | Install if missing |
|------|-----|--------------------|
| `ffmpeg` | Decodes the downloaded audio | `brew install ffmpeg` |
| `node` ≥ 20 **or** `deno` | yt-dlp needs a JavaScript runtime to read YouTube's player (required since yt-dlp 2025.11) | `brew install deno` |

Python packages (`requirements.txt`):

| Package | Role |
|---------|------|
| `yt-dlp[default,curl-cffi]` | Downloads audio from YouTube and Instagram. `default` adds `yt-dlp-ejs` (YouTube JS challenges); `curl-cffi` adds the browser impersonation Instagram needs |
| `youtube-transcript-api` | Reads YouTube's existing caption tracks in about a second, without downloading anything |
| `faster-whisper` | Silero VAD (speech detection) on every machine, and the Whisper backend on non-Apple machines |
| `mlx-whisper` | Whisper on the Apple Silicon GPU. Installed only on M-series Macs |
| `wcwidth` | Measures how many columns text really occupies (Hindi vowel signs, CJK, emoji) so lines wrap evenly |

**First run:** Whisper large-v3-turbo (about 1.6 GB) is downloaded once into `~/.cache/huggingface`.
The MLX copy is already cached on this Mac.

## 2. Run

```bash
.venv/bin/python transcript.py
```

Paste the URL when asked:

```
Enter YouTube or Instagram video URL: https://www.instagram.com/reel/DX7lzTOJ1p6/

Detected platform: Instagram (id: DX7lzTOJ1p6)
  -> Fetching Instagram post
  -> Transcribing 00:51 of speech (of 00:51 audio) with Whisper large-v3-turbo [mlx-whisper]

Success! Transcript generated in 6.2s
  Source   : Speech recognition (Whisper large-v3-turbo, mlx-whisper)
  Language : en
  Words    : 215
  Saved    : /Users/ayushgupta/IdeaProjects/LLD_2026/Transcript/instagram_transcript.txt
```

You never pick the platform yourself. You can also pass the URL directly:
`.venv/bin/python transcript.py "<url>"`.

Optional flags (none are needed for normal use):

| Flag | Effect |
|------|--------|
| `--timestamps` | Writes one `[mm:ss] text` line per segment instead of paragraphs |
| `--width 100` | Wraps lines at 100 columns instead of 80 (`--width 0` = no wrapping) |
| `--language hi` | Forces the spoken language (default: detected from the audio) |
| `--cookies-from-browser chrome` | Uses your logged-in browser session for private or age-restricted content (also `safari`, `firefox`, `edge`, `brave`, …) |
| `--cookies cookies.txt` | Netscape-format cookie file, as an alternative to the above |
| `--whisper` | YouTube: uses Whisper even for non-English videos that have YouTube auto-captions |
| `--fast` | YouTube: accepts auto-captions for English too. Faster, but less accurate |
| `--model large-v3` | Uses a different Whisper model. `turbo` is the default; `large-v3` is slower and a 3 GB download |
| `--engine faster-whisper` | Forces the CPU/CUDA backend instead of MLX |
| `--output-dir DIR` | Writes the file to another folder |
| `--debug` | Shows the full traceback for unexpected errors |

Supported URL forms:

| Platform | URL forms |
|----------|-----------|
| YouTube | `watch?v=`, `youtu.be/`, `/shorts/`, `/live/`, `/embed/`, `m.`, `music.`, `youtube-nocookie.com` |
| Instagram | `/reel/`, `/reels/`, `/p/`, `/tv/`, `/<user>/reel/…`, `/stories/<user>/<id>` (stories need cookies), `/share/…` links |

## 3. How it works and why

### URL detection (no false positives)
The URL is parsed with `urllib`, never matched as a substring:
- The host must be exactly the platform domain or a real subdomain of it. `notyoutube.com`,
  `youtube.com.evil.io` and `evil.io/?u=youtube.com/...` are all rejected.
- The video ID must have the right shape (11 characters for YouTube).
- Channel, playlist, search and profile pages get a clear "not a single video" error instead of a
  wrong guess.
- If you type an invalid URL at the prompt, the script asks again.

### YouTube pipeline (most accurate source first)
1. **Human-made captions in the spoken language.** These are the most accurate and take about 1–3 s.
   - Translation-only tracks are ignored. A Hindi video that only has English subtitles is *not*
     treated as having a transcript.
   - Captions that cover less than half of the video are rejected.
2. **No human captions:**
   - English → Whisper large-v3-turbo on the downloaded audio.
   - Other languages → YouTube's own auto-captions (override with `--whisper`).
3. **Fallback:** if the audio download or Whisper fails, auto-captions are used and labelled as such.

Some videos have AI-dubbed audio tracks, each with its own auto-caption track. For those, the
original language comes from yt-dlp's "original (default)" audio track, not from the first track in
the list.

### Instagram pipeline
Instagram does not expose captions publicly. The script downloads the audio-only stream (64 kbit/s,
usually under 1 MB) and runs Whisper on it. Posts with several videos are transcribed in order, marked
`[Video 1]`, `[Video 2]`, and so on.

### Speech recognition details
- **Silero VAD removes silence and music before Whisper runs.** This is faster and stops Whisper from
  inventing text. In testing, without VAD it produced a "Leonardo Silva Reviewer" line that is never
  spoken.
- **The language is detected from the audio, not taken from metadata.** See the Dutch case below.
- **Up to 440 tokens per 30 s window instead of Whisper's default 224.** This fixes the Hindi word
  loss below.
- **The compression-ratio check is off** because it misfires on non-Latin scripts. Repetition loops
  are removed by the script instead.
- **Engine:** MLX on Apple Silicon (GPU), faster-whisper (CTranslate2) everywhere else.

### Measurements on this Mac (M4 Pro)

| Test | Result |
|------|--------|
| TED talk, 14 min, English: Whisper turbo vs human captions | **WER 4.0%** (YouTube auto-captions: 6.2%) |
| Same talk, Whisper speed (MLX + VAD) | **~20 s** for 14 min of audio (~40× real time) |
| Hindi video, Whisper with default settings | **42% of words lost** (text cut mid-word, broken `�` characters) |
| Same Hindi video after the 440-token + compression fix | 496 vs 524 words, no broken characters, 2.5× faster |
| Same Hindi video, YouTube Hindi auto-captions | Correct spelling and punctuation; clearly better than Whisper, so this is the default for non-English |
| Dutch vlog tagged `en-US` by its uploader, with `en` forced | Whisper *translated* it into English (a false transcript) |
| Same Dutch vlog, language auto-detected | Correct Dutch transcript, plus a mismatch note in the terminal |

Typical end-to-end times (these depend on network speed):

| Case | Time |
|------|------|
| YouTube video with captions | 3–10 s |
| Instagram reel (51 s) | 6–11 s |
| YouTube Short (1.5 min, Whisper) | ~11 s |
| 10.7 min video (Whisper) | 35–50 s |

YouTube sometimes enforces a 2–14 s wait before an audio download starts. The script prints a note
when that happens.

### Output format
The file contains only the transcript text: paragraphs separated by a blank line, with each line
wrapped at **80 columns**, the same way code is kept inside a right margin. You never have to scroll
sideways.
```
So in college, I was a government major, which means I had to write a lot of
papers. Now, when a normal student writes a paper, they might spread the work
out a little like this. ...

And that would happen every single paper. ...
```
With `--timestamps`, continuation lines are indented under the text:
```
[01:05] So in college, I was a government major, which means I had to write a
        lot of papers.
```
Sound tags such as `[Music]`, `(Applause)` and `♪` are removed.

#### How lines are wrapped (and why not `textwrap`)
- **Column width, not character count.** In Hindi about 40% of code points are vowel signs and
  viramas that share a column with their letter. `len("इंटरव्यू")` is 8, but the word occupies 5
  columns. Wrapping by `len()` (as Python's `textwrap` does) would make Hindi lines look about 20%
  shorter than English ones. The script measures width with `wcwidth`, the library terminals use. If
  `wcwidth` is missing, it falls back to a Unicode rule.
- **Words are never split.** A word longer than the line (such as a URL) gets a line of its own.
  Punctuation such as `।` or `?` never starts a line.
- **Measured:** rendered with macOS fonts (Menlo, Monaco, JetBrains Mono, with the system's Devanagari
  fallback font), wrapped Hindi lines are at most 77–78 ASCII columns wide. They stay inside an 80-column
  margin in a real editor too.

## 4. Errors and fixes

For every expected failure the script prints `Error:` and a `Hint:`, exits with code 1, and writes
no file.

| Message | Cause | Fix |
|---------|-------|-----|
| `Unsupported website: …` | Not a YouTube or Instagram link | Paste a YouTube or Instagram video link |
| `… does not point to a single video / reel` | Channel, playlist, profile or search page | Open the video and copy its link |
| `Invalid YouTube video ID` | Truncated link | Copy the link again from the Share button |
| `Instagram did not return this post without logging in` | Private account, deleted post, or login wall | If you can see it while logged in: `--cookies-from-browser chrome` (or `safari`/`firefox`) |
| `This post does not contain a video` | Photo post | Nothing to transcribe |
| `This YouTube video is private` / `members-only` / `age-restricted` | Restricted access | `--cookies-from-browser chrome` with an account that has access |
| `confirm you are not a bot` / `HTTP 429` | Your IP is rate-limited | Wait, update yt-dlp, or use cookies |
| `live stream that is still running` / `upcoming premiere` | No finished recording yet | Try again after it ends |
| `The video is unavailable` | Deleted, region-blocked, or wrong ID | Check the link in a browser |
| `No speech was detected` | Music-only or silent video | Expected; no transcript is written |
| `Low recognition confidence` (note) | Noisy audio or wrong language | Try `--language <code>` |
| `YouTube lists the language as X, but the audio is Y` (note) | Wrong uploader metadata | Informational only; the transcript follows the audio |
| `yt-dlp could not read the video's media formats` | YouTube changed, or no JS runtime | `.venv/bin/pip install -U "yt-dlp[default,curl-cffi]"`, and make sure `node` or `deno` is installed |
| `ffmpeg is not installed` | Missing system tool | `brew install ffmpeg` |
| `Network error` | Offline, DNS problem or timeout | Check your connection and retry |

YouTube and Instagram change often. Most new breakages are fixed by updating:
```bash
.venv/bin/pip install -U "yt-dlp[default,curl-cffi]" youtube-transcript-api
```
The script reminds you when your yt-dlp is more than 60 days old.

**Cookies:** `--cookies-from-browser` reads your browser cookies locally for that one run and sends
them only to the site you asked for. Logged-in scraping can get an Instagram account rate-limited,
so use an account you are comfortable with.

## 5. Tests

```bash
.venv/bin/python -m unittest -v test_transcript.py
```
These are 22 offline tests. They cover:
- URL detection: valid forms, look-alike domains, non-video pages
- Text cleanup and repetition-loop removal
- Caption track selection: translation-only tracks, dubbed videos
- Timestamp mapping
- Error mapping
- Line wrapping (width limit, no lost or split words, Hindi danda, timestamp indent)
- Text-only output and the atomic write

## 6. Known limits
- **Instagram login wall:** Instagram increasingly hides posts from logged-out visitors. Public reels
  worked in testing without login; other posts need `--cookies-from-browser`.
- **Share links:** `instagram.com/share/…` links are resolved by following the redirect, but no real
  share link was available to test this.
- **Non-English YouTube without human captions:** the YouTube auto-captions default is based on the
  Hindi measurement. Other languages were not measured; use `--whisper` to compare.
- **Hinglish:** on mixed Hindi-English audio, Whisper writes everything in one script. Use
  `--language en` or `--language hi` to choose which.
