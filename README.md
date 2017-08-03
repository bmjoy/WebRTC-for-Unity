# WebRTC-for-Unity (WIP, NOT WORKING)
WebRTC for Unity (WIP, NOT WORKING)
This repo aims to facilitate WebRTC development in Unity, and maybe have a unifed API that behaves the same on different platforms.

## Roadmap:
- [x] Create an android plugin 
- [ ] Create an proxy to move I420 frames between the WebRTC and Unity (Textures)
    -   Creating a video capturer (Camera/Screenshare) and have it send a texture to Unity for rendering
    -   This would allow later to either stream the the video, or save it to disk (since WebRTC have everything needed to support encoding/decoding).
    -   Maybe have more options and flexibilites over this system (create a video stream from a render texture, hardware acceleration option; aka whether to use the GLES texture, or the YUV data of the frame)
- [ ] Get a simple video call POC in Unity
- [ ] Clean up code
- [ ] Create an abstracted WebRTC API for Unity, that can be implemented for each platform
- [ ] Support for iOS within the same API
- [ ] Support for WebGL (jslib plugin + polyfill maybe)
- [ ] Support for Standalone (Windows, OS X, Linux)
- [ ] Support for Editor (Should be easy, if standalone works)
- [ ] Create an interface API for signaling, that can be implemented for different ways of signaling (Websocket/Socket, Http, etc)