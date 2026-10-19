# HD Status — Android starter project

HD Status is a native Android starter app for preparing videos for WhatsApp Status sharing.

## Current MVP
- Pick a video from the Android system picker.
- Transcode it to MP4 with AndroidX Media3 Transformer.
- Fit the video into a 1080 × 1920 portrait canvas (1080p Full HD) without intentionally cropping the source.
- Save the output under `Movies/HD Status` and open Android's share sheet.

## Important limitations
- This is a **source project ZIP**, not a precompiled APK. It has not been built or device-tested in this environment.
- A 1080 × 1920 output canvas does not guarantee that the source contains native 1080p detail. Small videos may be upscaled.
- This MVP does not trim videos into Status-length clips, show a conversion percentage, or expose bitrate/FPS controls.
- Export support and performance depend on the Android device, available codecs, and free storage. Test with short clips first.
- WhatsApp may recompress videos after sharing. The app cannot guarantee WhatsApp's final playback quality or size limits.
- The output file is saved through MediaStore so the user can share it without broad storage permissions.

## Build a shareable debug APK using Android Studio
1. Install Android Studio: https://developer.android.com/studio
2. Extract this ZIP.
3. In Android Studio, choose **Open** and select the `HDStatusAndroid` folder.
4. Let Gradle sync. If prompted, install Android SDK Platform 35 and accept required licenses.
5. Connect an Android phone with USB debugging enabled, or use an emulator.
6. Press **Run** to install and test the app.
7. To build a shareable APK, choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
8. When the build completes, click **locate** in the notification. The debug APK is normally at:
   `app/build/outputs/apk/debug/app-debug.apk`
9. Send that APK to another Android user. They may need to allow installation from the app used to open the APK.

## Build from the command line
With Android Studio's Android SDK configured and Gradle installed, run from the project folder:
```bash
gradle assembleDebug
```
The APK should be at `app/build/outputs/apk/debug/app-debug.apk`.

## Before wider distribution
- Test on several real Android devices and with portrait, landscape, HDR, and large files.
- Add error handling and UX for low storage / unsupported codecs.
- For a release APK, use **Build > Generate Signed Bundle / APK** and keep the signing key private.
- Consider a privacy policy and clear disclosure of local video processing before publishing.


## Optional: build automatically on GitHub
The `.github/workflows/build-apk.yml` workflow builds the debug APK and uploads it as a workflow artifact.
1. Create a GitHub repository and upload the extracted project contents.
2. Open the repository's **Actions** tab.
3. Run **Build HD Status APK** (or push to `main`).
4. Open the completed workflow run and download the `HD-Status-debug-apk` artifact ZIP.
5. Extract it to get `app-debug.apk`.

This workflow is provided as build automation source; it has not been executed or verified here. The APK is only produced after a successful build run.
