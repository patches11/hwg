package com.hwg.webgl

object Shaders {
  val fShader: String =
    """
      |varying highp vec2 vTextureCoord;
      |varying highp vec3 vLightWeighting;
      |
      |uniform sampler2D uSampler;
      |
      |void main(void) {
      |  highp vec4 texelColor = texture2D(uSampler, vTextureCoord);
      |  gl_FragColor = vec4(texelColor.rgb * vLightWeighting, texelColor.a);
      |}
    """.stripMargin


  val vShader: String =
  """
    |attribute vec4 aVertexPosition;
    |attribute vec3 aVertexNormal;
    |attribute vec2 aTextureCoord;
    |
    |uniform mat4 uNormalMatrix;
    |uniform mat4 uModelViewMatrix;
    |uniform mat4 uProjectionMatrix;
    |
    |uniform vec3 uAmbientColor;
    |
    |uniform vec3 uLightingDirection;
    |uniform vec3 uDirectionalColor;
    |
    |varying highp vec2 vTextureCoord;
    |varying highp vec3 vLightWeighting;
    |
    |void main(void) {
    |  gl_Position = uProjectionMatrix * uModelViewMatrix * aVertexPosition;
    |  vTextureCoord = aTextureCoord;
    |
    |  highp vec4 transformedNormal = uNormalMatrix * vec4(aVertexNormal, 1.0);
    |
    |  highp float directional = max(dot(transformedNormal.xyz, normalize(uLightingDirection)), 0.0);
    |  vLightWeighting = uAmbientColor + (uDirectionalColor * directional);
    |}
  """.stripMargin
}
