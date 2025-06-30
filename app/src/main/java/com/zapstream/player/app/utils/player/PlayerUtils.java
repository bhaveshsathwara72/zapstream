package com.zapstream.player.app.utils.player;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.CaptioningManager;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory;
import androidx.media3.extractor.ts.TsExtractor;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.SubtitleView;
import androidx.nemosofts.utils.DeviceUtils;

import java.lang.reflect.Field;
import java.util.Locale;

import com.zapstream.player.app.utils.ApplicationUtil;

public class PlayerUtils {

    private static final String TAG = "PlayerUtils";

    private PlayerUtils() {
        throw new IllegalStateException("Utility class");
    }

    @OptIn(markerClass = UnstableApi.class)
    @NonNull
    public static DefaultExtractorsFactory getDefaultExtractorsFactory() {
        return new DefaultExtractorsFactory()
                .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS
                        | DefaultTsPayloadReaderFactory.FLAG_IGNORE_AAC_STREAM
                        | DefaultTsPayloadReaderFactory.FLAG_IGNORE_H264_STREAM)
                .setMatroskaExtractorFlags(0)
                .setMp4ExtractorFlags(0)
                .setTsExtractorTimestampSearchBytes(2500 * TsExtractor.TS_PACKET_SIZE);
    }

    @OptIn(markerClass = UnstableApi.class)
    @NonNull
    public static DefaultRenderersFactory getDefaultRenderersFactory(Context context, @NonNull Boolean isHardwareDecoding) {
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context);
        if (Boolean.TRUE.equals(isHardwareDecoding)){
            renderersFactory.setEnableDecoderFallback(true); // Allow fallback to software decoder if necessary
            renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER); // Prefer hardware
        } else {
            renderersFactory.setEnableDecoderFallback(false); // No fallback to hardware decoders
            renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF); // Force software decoding
        }
        return renderersFactory;
    }

    @NonNull
    @OptIn(markerClass = UnstableApi.class)
    public static DefaultTrackSelector getDefaultTrackSelector(Context context) {
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
        trackSelector.setParameters(trackSelector.buildUponParameters()
                .setPreferredVideoMimeTypes(
                        MimeTypes.VIDEO_H263,
                        MimeTypes.VIDEO_H264,
                        MimeTypes.VIDEO_H265,
                        MimeTypes.VIDEO_VP8,
                        MimeTypes.VIDEO_VP9,
                        MimeTypes.VIDEO_AV1,
                        MimeTypes.VIDEO_MATROSKA,
                        MimeTypes.VIDEO_MP4,
                        MimeTypes.VIDEO_WEBM,
                        MimeTypes.VIDEO_MPEG,
                        MimeTypes.VIDEO_FLV,
                        MimeTypes.VIDEO_AVI,
                        MimeTypes.VIDEO_UNKNOWN
                )
                .setPreferredAudioMimeTypes(
                        MimeTypes.AUDIO_AAC,    // AAC (LC, ELD, HE, xHE)
                        MimeTypes.AUDIO_AC3,    // AC-3
                        MimeTypes.AUDIO_E_AC3,  // Enhanced AC-3 (EAC-3)
                        MimeTypes.AUDIO_FLAC,   // FLAC
                        MimeTypes.AUDIO_MPEG,   // MP3, MP2, MP1
                        MimeTypes.AUDIO_OPUS,   // Opus
                        MimeTypes.AUDIO_VORBIS, // Vorbis
                        MimeTypes.AUDIO_ALAC,   // Apple Lossless (ALAC)
                        MimeTypes.AUDIO_WAV,    // PCM/WAV
                        MimeTypes.AUDIO_DTS,    // DTS
                        MimeTypes.AUDIO_DTS_HD, // DTS-HD
                        MimeTypes.AUDIO_TRUEHD   // Dolby TrueHD
                )
                .setAllowVideoMixedMimeTypeAdaptiveness(true)
                .setAllowMultipleAdaptiveSelections(true) // Enable adaptive selection
        );

        // Set captioning parameters if enabled
        final CaptioningManager captioningManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        if (!captioningManager.isEnabled()) {
            trackSelector.setParameters(trackSelector.buildUponParameters().setIgnoredTextSelectionFlags(C.SELECTION_FLAG_DEFAULT));
        }
        Locale locale = captioningManager.getLocale();
        if (locale != null) {
            trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredTextLanguage(locale.getISO3Language()));
        }
        return trackSelector;
    }

    @OptIn(markerClass = UnstableApi.class)
    public static void setCustomTrackNameProvider(Context context, CustomPlayerView playerView) {
        if (context == null || playerView == null) {
            return;
        }
        try {
            PlayerControlView controlView = playerView.findViewById(androidx.media3.ui.R.id.exo_controller);
            CustomDefaultTrackNameProvider trackNameProvider = new CustomDefaultTrackNameProvider(context.getResources());

            final Field field = PlayerControlView.class.getDeclaredField("trackNameProvider");
            field.setAccessible(true);
            field.set(controlView, trackNameProvider);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set custom track name provider", e);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public static void setCustomSubtitle(Context context, CustomPlayerView playerView) {
        if (context == null || playerView == null) {
            return;
        }

        // Set custom subtitle view style
        try {
            final CaptioningManager mCaptioningManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
            if (mCaptioningManager == null){
                return;
            }
            final SubtitleView subtitleView = playerView.getSubtitleView();
            final boolean isTablet = DeviceUtils.isTablet(context);
            final boolean isTvBox = DeviceUtils.isTvBox(context);
            float subtitlesScale = normalizeFontScale(mCaptioningManager.getFontScale(), isTvBox || isTablet);
            if (subtitleView == null) {
                return;
            }
            final CaptioningManager.CaptionStyle userStyle = mCaptioningManager.getUserStyle();
            final CaptionStyleCompat userStyleCompat = CaptionStyleCompat.createFromCaptionStyle(userStyle);
            final CaptionStyleCompat captionStyle = new CaptionStyleCompat(
                    userStyle.hasForegroundColor() ? userStyleCompat.foregroundColor : Color.WHITE,
                    userStyle.hasBackgroundColor() ? userStyleCompat.backgroundColor : Color.TRANSPARENT,
                    userStyle.hasWindowColor() ? userStyleCompat.windowColor : Color.TRANSPARENT,
                    userStyle.hasEdgeType() ? userStyleCompat.edgeType : CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                    userStyle.hasEdgeColor() ? userStyleCompat.edgeColor : Color.BLACK,
                    Typeface.create(userStyleCompat.typeface != null ? userStyleCompat.typeface : Typeface.DEFAULT, Typeface.NORMAL));
            subtitleView.setStyle(captionStyle);
            subtitleView.setApplyEmbeddedStyles(false);
            subtitleView.setBottomPaddingFraction(SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION * 2f / 3f);

            // Tweak text size as fraction size doesn't work well in portrait
            final float size = getaFloat(context, subtitlesScale);
            subtitleView.setFractionalTextSize(size);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to set custom subtitle view style", e);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private static float getaFloat(@NonNull Context context, float subtitlesScale) {
        final float size;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            size = SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * subtitlesScale;
        } else {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            float ratio = ((float)metrics.heightPixels / (float)metrics.widthPixels);
            if (ratio < 1)
                ratio = 1 / ratio;
            size = SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * subtitlesScale / ratio;
        }
        return size;
    }

    private static float normalizeFontScale(float fontScale, boolean small) {
        // https://bbc.github.io/subtitle-guidelines/#Presentation-font-size
        if (fontScale >= 1.99f) {
            return small ? 1.15f : 1.2f;
        }
        if (fontScale > 1.01f) {
            return small ? 1.0f : 1.1f;
        }

        // Handle small font scales
        if (fontScale <= 0.26f) {
            return small ? 0.65f : 0.8f;
        }
        if (fontScale < 0.99f) {
            return small ? 0.75f : 0.9f;
        }

        // Default case for font scale ~1.0
        return small ? 0.85f : 1.0f;
    }
}