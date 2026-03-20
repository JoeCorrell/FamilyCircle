#!/bin/bash
# Download Outfit and Space Mono fonts from Google Fonts
# Run this script from the HavenApp root directory

FONT_DIR="app/src/main/res/font"
mkdir -p "$FONT_DIR"

echo "Downloading Outfit font family..."
# Outfit
curl -L "https://github.com/nicholasjuncos/Outfit/raw/main/fonts/ttf/Outfit-Regular.ttf" -o "$FONT_DIR/outfit_regular.ttf"
curl -L "https://github.com/nicholasjuncos/Outfit/raw/main/fonts/ttf/Outfit-Medium.ttf" -o "$FONT_DIR/outfit_medium.ttf"
curl -L "https://github.com/nicholasjuncos/Outfit/raw/main/fonts/ttf/Outfit-SemiBold.ttf" -o "$FONT_DIR/outfit_semibold.ttf"
curl -L "https://github.com/nicholasjuncos/Outfit/raw/main/fonts/ttf/Outfit-Bold.ttf" -o "$FONT_DIR/outfit_bold.ttf"
curl -L "https://github.com/nicholasjuncos/Outfit/raw/main/fonts/ttf/Outfit-ExtraBold.ttf" -o "$FONT_DIR/outfit_extrabold.ttf"
curl -L "https://github.com/nicholasjuncos/Outfit/raw/main/fonts/ttf/Outfit-Black.ttf" -o "$FONT_DIR/outfit_black.ttf"

echo "Downloading Space Mono font family..."
# Space Mono
curl -L "https://github.com/googlefonts/spacemono/raw/main/fonts/SpaceMono-Regular.ttf" -o "$FONT_DIR/spacemono_regular.ttf"
curl -L "https://github.com/googlefonts/spacemono/raw/main/fonts/SpaceMono-Bold.ttf" -o "$FONT_DIR/spacemono_bold.ttf"

echo "Fonts downloaded to $FONT_DIR"
echo ""
echo "If the download fails, manually download the fonts from:"
echo "  - Outfit: https://fonts.google.com/specimen/Outfit"
echo "  - Space Mono: https://fonts.google.com/specimen/Space+Mono"
echo ""
echo "Place them in $FONT_DIR with these exact names:"
echo "  outfit_regular.ttf, outfit_medium.ttf, outfit_semibold.ttf,"
echo "  outfit_bold.ttf, outfit_extrabold.ttf, outfit_black.ttf,"
echo "  spacemono_regular.ttf, spacemono_bold.ttf"
