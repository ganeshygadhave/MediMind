import os
from PIL import Image

def generate_icons(source_image_path, target_res_dir):
    sizes = {
        "mdpi": 48,
        "hdpi": 72,
        "xhdpi": 96,
        "xxhdpi": 144,
        "xxxhdpi": 192
    }
    
    img = Image.open(source_image_path)
    
    for density, size in sizes.items():
        # ic_launcher
        mipmap_dir = os.path.join(target_res_dir, f"mipmap-{density}")
        os.makedirs(mipmap_dir, exist_ok=True)
        
        resized_img = img.resize((size, size), Image.Resampling.LANCZOS)
        
        # Save square/normal icon
        resized_img.save(os.path.join(mipmap_dir, "ic_launcher.png"))
        
        # Save round icon (for simplicity, we'll just save the same image if it's already round-friendly, 
        # but to be safe, we'll just save it as ic_launcher_round.png as well)
        resized_img.save(os.path.join(mipmap_dir, "ic_launcher_round.png"))
        
    print("Icons generated successfully!")

if __name__ == "__main__":
    generate_icons(
        r"c:\MediMind\medimind_app_logo.png",
        r"c:\MediMind\android\app\src\main\res"
    )
