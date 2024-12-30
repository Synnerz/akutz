#version 110

uniform float u_OutlineWidth;
uniform vec4 u_InnerRect;
uniform float u_Radius;

varying vec2 f_Position;

float sdRoundBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

void main() {
    vec2 bottomLeft = u_InnerRect.xy + u_OutlineWidth;
    vec2 topRight = u_InnerRect.zw - u_OutlineWidth;
    vec2 center = (bottomLeft + topRight) * 0.5;
    vec2 size = (topRight - bottomLeft) * 0.5;
    vec2 roundedPosition = f_Position - center;
    float distance = sdRoundBox(roundedPosition, size, u_Radius);
    float outline = 1.0 - smoothstep(0.0, u_OutlineWidth, abs(distance));

    if (outline == 0.0) {
        discard;
    }

    gl_FragColor = gl_Color * vec4(1.0, 1.0, 1.0, outline);
}
