Shader "Unlit/Unlit Texture No Fog"
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
             }
             #endif
            
             #ifdef FRAGMENT
             // require GL_OES_EGL_image_external so we can access the external texture data on android's GPU
             #extension GL_OES_EGL_image_external : require
             //precision mediump float; // added
             varying vec2 TextureCoordinate;
             //uniform sampler2D _MainTex;
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
