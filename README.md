# WebRTC-for-Unity (WIP, NOT WORKING)
WebRTC for Unity (WIP, NOT WORKING)
This repo aims to facilitate WebRTC development in Unity, and maybe have a unifed API that behaves the same on different platforms.

## Current state:
Right now there is something weird going on with rendering the textures, as you can see in the image.
One cube is supposed to show the screenshare texture, the other is supposed to show the camera.
For some reason, it is showing the silhouette of the two cubes. Go figure.

This should have to do with EGL context and sharing textures, threading, and/or frame rendering.
I know we're getting a valid textureId out of the frames, but I can't figure out why it's not rendering properly.

***This is using the Stardard shader. With a Unlit Texture shader, the cubes are white (In some other cases the cubes are red)***

<img src="https://dl2.pushbulletusercontent.com/GhJCzRvohgtAZiYhbetqBdUjTMhsP77N/Screenshot_20170803-094159.png" width="200" />

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

## Contribution:
Yes, please. If you think you can contribute to any of the points above, or have any suggestions, design thoughts, use cases, anything really, go ahead and open an issue and/or create a pull request.
