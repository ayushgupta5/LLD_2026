"""Offline unit tests. Run: .venv/bin/python -m unittest -v test_transcript.py"""

import tempfile
import unittest
from pathlib import Path

import transcript as t


class DetectPlatformTests(unittest.TestCase):
    YT_ID = "dQw4w9WgXcQ"

    def assertYouTube(self, url):
        ref = t.detect_platform(url)
        self.assertEqual(ref.platform, "youtube", url)
        self.assertEqual(ref.video_id, self.YT_ID, url)
        self.assertEqual(ref.url, f"https://www.youtube.com/watch?v={self.YT_ID}")

    def assertInstagram(self, url, code, canonical):
        ref = t.detect_platform(url)
        self.assertEqual(ref.platform, "instagram", url)
        self.assertEqual(ref.video_id, code, url)
        self.assertEqual(ref.url, canonical, url)

    def assertRejected(self, url):
        with self.assertRaises(t.TranscriptError, msg=url):
            t.detect_platform(url)

    def test_youtube_variants(self):
        for url in [
            f"https://www.youtube.com/watch?v={self.YT_ID}",
            f"https://youtube.com/watch?v={self.YT_ID}&t=42s&list=PL123",
            f"https://m.youtube.com/watch?feature=share&v={self.YT_ID}",
            f"https://music.youtube.com/watch?v={self.YT_ID}",
            f"https://youtu.be/{self.YT_ID}?si=abcdef",
            f"youtu.be/{self.YT_ID}",
            f"www.youtube.com/shorts/{self.YT_ID}",
            f"https://www.youtube.com/live/{self.YT_ID}?feature=share",
            f"https://www.youtube.com/embed/{self.YT_ID}",
            f"https://www.youtube-nocookie.com/embed/{self.YT_ID}",
            f"  <https://WWW.YOUTUBE.COM/watch?v={self.YT_ID}>  ",
            f"http://www.youtube.com./watch?v={self.YT_ID}",
        ]:
            self.assertYouTube(url)

    def test_instagram_variants(self):
        canon_reel = "https://www.instagram.com/reel/DX7lzTOJ1p6/"
        self.assertInstagram("https://www.instagram.com/reel/DX7lzTOJ1p6/", "DX7lzTOJ1p6", canon_reel)
        self.assertInstagram("https://instagram.com/reels/DX7lzTOJ1p6?igsh=xyz", "DX7lzTOJ1p6", canon_reel)
        self.assertInstagram("instagram.com/someuser/reel/DX7lzTOJ1p6/", "DX7lzTOJ1p6", canon_reel)
        self.assertInstagram("https://www.instagram.com/p/DX7lzTOJ1p6/?img_index=1", "DX7lzTOJ1p6",
                             "https://www.instagram.com/p/DX7lzTOJ1p6/")
        self.assertInstagram("https://m.instagram.com/tv/CAbcdEfGhIj/", "CAbcdEfGhIj",
                             "https://www.instagram.com/tv/CAbcdEfGhIj/")
        ref = t.detect_platform("https://www.instagram.com/stories/natgeo/3456789012345678901/")
        self.assertEqual((ref.platform, ref.video_id), ("instagram", "3456789012345678901"))

    def test_rejects_unsupported_and_lookalikes(self):
        for url in [
            "",
            "   ",
            "hello world",
            "https://vimeo.com/123456",
            f"https://notyoutube.com/watch?v={self.YT_ID}",
            f"https://youtube.com.evil.io/watch?v={self.YT_ID}",
            f"https://evil.io/?u=https://youtube.com/watch?v={self.YT_ID}",
            f"https://evil.io/youtube.com/watch?v={self.YT_ID}",
            "https://fakeinstagram.com/reel/DX7lzTOJ1p6/",
            "https://instagram.com.attacker.net/reel/DX7lzTOJ1p6/",
            f"ftp://youtube.com/watch?v={self.YT_ID}",
            "javascript:alert(1)",
            "https://www.facebook.com/reel/123",
        ]:
            self.assertRejected(url)

    def test_rejects_non_video_pages(self):
        for url in [
            "https://www.youtube.com/",
            "https://www.youtube.com/@mkbhd",
            "https://www.youtube.com/playlist?list=PL123",
            "https://www.youtube.com/results?search_query=python",
            "https://www.youtube.com/watch",
            "https://www.youtube.com/watch?v=short",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQextra",
            "https://youtu.be/",
            "https://www.instagram.com/",
            "https://www.instagram.com/natgeo/",
            "https://www.instagram.com/explore/tags/python/",
            "https://www.instagram.com/reel/",
            "https://www.instagram.com/reel/bad$code/",
            "https://youtube.com:99999/watch?v=dQw4w9WgXcQ",
        ]:
            self.assertRejected(url)


class TextTests(unittest.TestCase):
    def test_clean_text_removes_non_speech(self):
        self.assertEqual(t.clean_text("[Music] Hello &amp; welcome\nback (Applause)"), "Hello & welcome back")
        self.assertEqual(t.clean_text("♪♪♪"), "")
        self.assertEqual(t.clean_text(">> Next speaker"), "Next speaker")
        self.assertEqual(t.clean_text("This (part) stays"), "This (part) stays")

    def test_tidy_collapses_repetition_loops(self):
        segs = [t.Segment(i, i + 1, "Thank you.") for i in range(5)] + [t.Segment(6, 7, "Bye")]
        out = t.tidy_segments(segs)
        self.assertEqual([s.text for s in out], ["Thank you.", "Thank you.", "Bye"])
        self.assertEqual(out[1].end, 5)

    def test_collapse_repeats_only_removes_loops(self):
        self.assertEqual(t.collapse_repeats("एकनल समय समय समय समय समय मैं"), "एकनल समय मैं")
        self.assertEqual(t.collapse_repeats("a b a b a b a b c"), "a b c")
        self.assertEqual(t.collapse_repeats("very very good"), "very very good")
        self.assertEqual(t.collapse_repeats("Thank you. Thank you."), "Thank you. Thank you.")

    def test_paragraphs_break_on_pause(self):
        long_text = " ".join(["word"] * 35) + "."
        segs = [t.Segment(0, 10, long_text), t.Segment(13, 14, "New topic.")]
        self.assertEqual(len(t.build_paragraphs(segs)), 2)
        self.assertEqual(len(t.build_paragraphs([t.Segment(0, 1, "a"), t.Segment(5, 6, "b")])), 1)

    def test_speech_map_restores_original_times(self):
        m = t.SpeechMap([(0.0, 10.0, 5.0), (5.0, 30.0, 4.0)])
        self.assertAlmostEqual(m.to_original(2.0), 12.0)
        self.assertAlmostEqual(m.to_original(6.0), 31.0)
        self.assertAlmostEqual(m.to_original(100.0), 34.0)  # clamped to the end of the last chunk

    def test_whisper_language(self):
        self.assertEqual(t.whisper_language("en-US"), "en")
        self.assertEqual(t.whisper_language("hi"), "hi")
        self.assertEqual(t.whisper_language("iw"), "he")
        self.assertIsNone(t.whisper_language("xx-unknown"))
        self.assertIsNone(t.whisper_language(None))

    def test_caption_coverage(self):
        segs = [t.Segment(0, 5, "a"), t.Segment(50, 55, "b")]
        self.assertTrue(t.captions_cover_video(segs, 100))
        self.assertFalse(t.captions_cover_video(segs, 600))
        self.assertTrue(t.captions_cover_video(segs, None))
        self.assertFalse(t.captions_cover_video([], 100))

    def test_output_file_has_only_transcript_text(self):
        tr = t.Transcript([t.Segment(0, 1, "Hello there."), t.Segment(65, 66, "Bye.")], method="x",
                          language="en", notes=["a note"])
        self.assertEqual(t.render_transcript(tr, False), "Hello there. Bye.\n")
        self.assertEqual(t.render_transcript(tr, True), "[00:00] Hello there.\n[01:05] Bye.\n")

    def test_atomic_write(self):
        with tempfile.TemporaryDirectory() as d:
            p = Path(d) / "out.txt"
            t.write_atomically(p, "héllo\n")
            self.assertEqual(p.read_text(encoding="utf-8"), "héllo\n")
            self.assertEqual([x.name for x in Path(d).iterdir()], ["out.txt"])


class TrackSelectionTests(unittest.TestCase):
    class Track:
        def __init__(self, code, generated):
            self.language_code, self.is_generated = code, generated

    def test_prefers_spoken_language_human_track(self):
        tracks = [self.Track("ar", False), self.Track("en", False), self.Track("en", True)]
        self.assertEqual(t.pick_human_track(tracks, "en").language_code, "en")

    def test_ignores_translation_only_tracks(self):
        tracks = [self.Track("en", False), self.Track("hi", True)]  # Hindi video, English subs only
        self.assertIsNone(t.pick_human_track(tracks, "hi"))

    def test_original_audio_language_for_dubbed_videos(self):
        info = {"formats": [
            {"language": "de-DE", "language_preference": -1, "acodec": "opus"},
            {"language": "en-US", "language_preference": 10, "acodec": "opus"},  # "original (default)"
            {"language": "xx", "acodec": "none"},
        ]}
        self.assertEqual(t.original_audio_language(info), "en-US")
        self.assertEqual(t.original_audio_language({"language": "fr"}), "fr")
        generated = [self.Track("ar", True), self.Track("en", True), self.Track("hi", True)]
        self.assertEqual(t.pick_track(generated, "en-US").language_code, "en")

    def test_unknown_language_needs_single_track(self):
        self.assertEqual(t.pick_human_track([self.Track("en-GB", False)], None).language_code, "en-GB")
        self.assertIsNone(t.pick_human_track([self.Track("en", False), self.Track("de", False)], None))


class ErrorMappingTests(unittest.TestCase):
    def test_maps_known_errors(self):
        e = t.explain_download_error(Exception("ERROR: [youtube] abc: Private video. Sign in"), "youtube")
        self.assertIn("private", str(e).lower())
        e = t.explain_download_error(Exception("ERROR: [youtube] abc: This video is unavailable"), "youtube")
        self.assertIn("unavailable", str(e))
        e = t.explain_download_error(Exception("ERROR: [Instagram] x: Instagram sent an empty media response."),
                                     "instagram")
        self.assertIn("Instagram", str(e))
        self.assertIn("--cookies-from-browser", e.hint)
        e = t.explain_download_error(Exception("something brand new"), "youtube")
        self.assertIn("Download failed", str(e))


if __name__ == "__main__":
    unittest.main()
