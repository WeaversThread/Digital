// Renders the macOS app icon master (1024×1024 PNG).
//
//   swift make-icon.swift <artwork.png | -> <out.png>
//
// The artwork is full-bleed square art; it is scaled onto the macOS icon grid
// (824 pt body, 100 pt margin), clipped to the rounded-square shape and given
// the standard soft shadow. Without artwork ("-") a placeholder is drawn.

import CoreGraphics
import Foundation
import ImageIO
import UniformTypeIdentifiers

let size = 1024
let body = CGRect(x: 100, y: 100, width: 824, height: 824)
let radius: CGFloat = 185

let args = CommandLine.arguments
guard args.count == 3 else {
    FileHandle.standardError.write("usage: make-icon.swift <artwork.png | -> <out.png>\n".data(using: .utf8)!)
    exit(1)
}

let space = CGColorSpace(name: CGColorSpace.sRGB)!
let ctx = CGContext(data: nil, width: size, height: size, bitsPerComponent: 8, bytesPerRow: 0,
                    space: space, bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue)!
ctx.interpolationQuality = .high

let shape = CGPath(roundedRect: body, cornerWidth: radius, cornerHeight: radius, transform: nil)

// soft shadow below the body, as in Apple's template
ctx.saveGState()
ctx.setShadow(offset: CGSize(width: 0, height: -10), blur: 24, color: CGColor(gray: 0, alpha: 0.35))
ctx.addPath(shape)
ctx.setFillColor(CGColor(gray: 0.2, alpha: 1))
ctx.fillPath()
ctx.restoreGState()

ctx.saveGState()
ctx.addPath(shape)
ctx.clip()
if args[1] != "-" {
    let url = URL(fileURLWithPath: args[1]) as CFURL
    guard let src = CGImageSourceCreateWithURL(url, nil),
          let art = CGImageSourceCreateImageAtIndex(src, 0, nil) else {
        FileHandle.standardError.write("cannot read \(args[1])\n".data(using: .utf8)!)
        exit(1)
    }
    ctx.draw(art, in: body)
} else {
    // placeholder: dark glass with a thin white AND gate and an orange output
    ctx.setFillColor(CGColor(srgbRed: 0.118, green: 0.118, blue: 0.118, alpha: 1))
    ctx.fill(body)
    ctx.setStrokeColor(CGColor(gray: 1, alpha: 0.85))
    ctx.setLineWidth(22)
    ctx.setLineCap(.round)
    ctx.setLineJoin(.round)
    let gate = CGMutablePath()
    gate.move(to: CGPoint(x: 512, y: 312))
    gate.addLine(to: CGPoint(x: 352, y: 312))
    gate.addLine(to: CGPoint(x: 352, y: 712))
    gate.addLine(to: CGPoint(x: 512, y: 712))
    gate.addArc(center: CGPoint(x: 512, y: 512), radius: 200, startAngle: .pi / 2, endAngle: -.pi / 2, clockwise: true)
    ctx.addPath(gate)
    for y in [412, 612] {
        ctx.move(to: CGPoint(x: 232, y: y))
        ctx.addLine(to: CGPoint(x: 352, y: y))
    }
    ctx.strokePath()
    ctx.setStrokeColor(CGColor(srgbRed: 1, green: 0.42, blue: 0.1, alpha: 1))
    ctx.move(to: CGPoint(x: 712, y: 512))
    ctx.addLine(to: CGPoint(x: 812, y: 512))
    ctx.strokePath()
}
ctx.restoreGState()

// hairline rim so the shape reads on dark docks
ctx.addPath(shape)
ctx.setStrokeColor(CGColor(gray: 1, alpha: 0.12))
ctx.setLineWidth(2)
ctx.strokePath()

let out = URL(fileURLWithPath: args[2]) as CFURL
let dest = CGImageDestinationCreateWithURL(out, UTType.png.identifier as CFString, 1, nil)!
CGImageDestinationAddImage(dest, ctx.makeImage()!, nil)
guard CGImageDestinationFinalize(dest) else { exit(1) }
