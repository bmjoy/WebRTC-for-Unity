Shader "WebRTC/VideoShader"
{
	Properties
	{
		_MainTex ("Texture", 2D) = "white" {}
	}

	SubShader
	{
		Tags { "RenderType"="Opaque" }
		LOD 100

         Pass 
         {
             GLSLPROGRAM       
 
             #ifdef VERTEX
             varying vec2 TextureCoordinate;
             void main()
             {
                 gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
                 TextureCoordinate = gl_MultiTexCoord0.xy;
                 TextureCoordinate.y = 1.0 - TextureCoordinate.y;
             }
             #endif
            
             #ifdef FRAGMENT
             #extension GL_OES_EGL_image_external : require
             varying vec2 TextureCoordinate;
             uniform samplerExternalOES _MainTex; // replaced above line with this in order to use GL_OES_EGL_image_external
            
             void main()
             {
                 gl_FragColor = texture2D(_MainTex, TextureCoordinate);
             }
             #endif
 
             ENDGLSL
         }
	}

	FallBack "Unlit/Texture"
}
