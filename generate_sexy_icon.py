import os
import math
from PIL import Image, ImageDraw, ImageFilter, ImageFont

def create_synthwave_icon(size):
    width, height = size
    img = Image.new('RGBA', size, (15, 2, 27, 255))
    draw = ImageDraw.Draw(img)
    
    # Draw background gradient (dark purple to almost black)
    for y in range(height):
        r = int(15 + (10 - 15) * (y / height))
        g = int(2 + (0 - 2) * (y / height))
        b = int(27 + (40 - 27) * (y / height))
        draw.line([(0, y), (width, y)], fill=(r, g, b, 255))
        
    # Draw Synthwave Sun
    sun_radius = int(width * 0.35)
    sun_cx = width // 2
    sun_cy = int(height * 0.45)
    
    sun = Image.new('RGBA', size, (0,0,0,0))
    sun_draw = ImageDraw.Draw(sun)
    
    # Sun gradient (Yellow to Magenta)
    for y in range(sun_cy - sun_radius, sun_cy + sun_radius):
        if y < 0 or y >= height: continue
        progress = (y - (sun_cy - sun_radius)) / (sun_radius * 2)
        r = int(255)
        g = int(220 - 220 * progress)
        b = int(0 + 127 * progress)
        
        # Calculate x bounds for the circle
        dy = y - sun_cy
        dx = math.sqrt(max(0, sun_radius**2 - dy**2))
        
        # Add retro sun horizontal cuts
        if progress > 0.5:
            cut_freq = int(20 * (height/1024))
            if (y % cut_freq) < (cut_freq * ((progress - 0.5) * 2)):
                continue
                
        sun_draw.line([(sun_cx - dx, y), (sun_cx + dx, y)], fill=(r, g, b, 255))
        
    # Add glow to sun
    sun_glow = sun.filter(ImageFilter.GaussianBlur(radius=int(width*0.03)))
    img.paste(sun_glow, (0,0), sun_glow)
    img.paste(sun, (0,0), sun)
    
    # Draw Cyan Grid
    grid_color = (0, 245, 255, 200)
    grid_cy = int(height * 0.65)
    
    for y in range(grid_cy, height, int(height*0.04)):
        thickness = max(1, int(3 * ((y - grid_cy) / (height - grid_cy))))
        draw.line([(0, y), (width, y)], fill=grid_color, width=thickness)
        
    for x in range(0, width, int(width*0.08)):
        offset = x - (width//2)
        # perspective lines
        x1 = (width//2) + offset
        x2 = (width//2) + int(offset * 3)
        draw.line([(x1, grid_cy), (x2, height)], fill=grid_color, width=2)

    # Draw Neon "J"
    j_img = Image.new('RGBA', size, (0,0,0,0))
    j_draw = ImageDraw.Draw(j_img)
    
    j_width = int(width * 0.2)
    j_height = int(height * 0.4)
    j_x = width // 2 - j_width // 2
    j_y = height // 2 - j_height // 2
    
    # Custom J path
    j_path = [
        (j_x + j_width, j_y),
        (j_x + j_width, j_y + j_height - j_width),
    ]
    j_draw.line(j_path, fill=(255, 255, 255, 255), width=int(width*0.06), joint="curve")
    j_draw.arc([j_x, j_y + j_height - j_width*2, j_x + j_width*2, j_y + j_height], 0, 180, fill=(255, 255, 255, 255), width=int(width*0.06))
    
    # Glow for J
    j_glow = j_img.filter(ImageFilter.GaussianBlur(radius=int(width*0.02)))
    # Tint glow hot pink
    glow_data = j_glow.getdata()
    new_glow = []
    for item in glow_data:
        if item[3] > 0:
            new_glow.append((255, 0, 127, item[3]))
        else:
            new_glow.append(item)
    j_glow.putdata(new_glow)
    
    img.paste(j_glow, (0,0), j_glow)
    img.paste(j_img, (0,0), j_img)

    return img

def main():
    os.makedirs('app/src/main/res/drawable', exist_ok=True)
    
    base_size = 1024
    master_icon = create_synthwave_icon((base_size, base_size))
    master_icon.save('app/src/main/res/drawable/joey_official.png', 'PNG')
    master_icon.save('app/src/main/res/drawable/joey_avatar.png', 'PNG')
    
    sizes = {
        'mipmap-mdpi': (48, 48),
        'mipmap-hdpi': (72, 72),
        'mipmap-xhdpi': (96, 96),
        'mipmap-xxhdpi': (144, 144),
        'mipmap-xxxhdpi': (192, 192)
    }

    for folder, size in sizes.items():
        os.makedirs(f'app/src/main/res/{folder}', exist_ok=True)
        # Square with slight rounded corners
        resized = master_icon.resize(size, Image.Resampling.LANCZOS)
        resized.save(f'app/src/main/res/{folder}/ic_launcher.png', 'PNG')
        
        # Circular for ic_launcher_round
        mask = Image.new('L', size, 0)
        draw = ImageDraw.Draw(mask)
        draw.ellipse((0, 0) + size, fill=255)
        round_img = Image.new('RGBA', size, (0, 0, 0, 0))
        round_img.paste(resized, (0, 0), mask=mask)
        
        draw_border = ImageDraw.Draw(round_img)
        draw_border.ellipse((1, 1, size[0]-2, size[1]-2), outline=(0, 245, 255, 255), width=max(1, size[0]//32))
        round_img.save(f'app/src/main/res/{folder}/ic_launcher_round.png', 'PNG')

    # Adaptive foreground
    adaptive_fg = Image.new('RGBA', (432, 432), (0, 0, 0, 0))
    fg_size = 280
    fg_img = master_icon.resize((fg_size, fg_size), Image.Resampling.LANCZOS)
    
    mask_fg = Image.new('L', (fg_size, fg_size), 0)
    draw_fg = ImageDraw.Draw(mask_fg)
    draw_fg.ellipse((0, 0, fg_size, fg_size), fill=255)

    circ_fg = Image.new('RGBA', (fg_size, fg_size), (0, 0, 0, 0))
    circ_fg.paste(fg_img, (0, 0), mask=mask_fg)

    draw_rings = ImageDraw.Draw(circ_fg)
    draw_rings.ellipse((2, 2, fg_size-3, fg_size-3), outline=(0, 245, 255, 255), width=4)
    draw_rings.ellipse((6, 6, fg_size-7, fg_size-7), outline=(255, 0, 127, 200), width=2)

    offset_x = (432 - fg_size) // 2
    offset_y = (432 - fg_size) // 2
    adaptive_fg.paste(circ_fg, (offset_x, offset_y), circ_fg)
    adaptive_fg.save('app/src/main/res/drawable/ic_launcher_foreground.png', 'PNG')

    print("Successfully generated high-quality sexy synthwave icons!")

if __name__ == "__main__":
    main()
