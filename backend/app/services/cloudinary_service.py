"""
MedRem Backend — Cloudinary Service
File upload and management via Cloudinary.
"""

import cloudinary
import cloudinary.uploader
from fastapi import UploadFile

from app.config import settings


def init_cloudinary():
    """Initialize Cloudinary configuration."""
    cloudinary.config(
        cloud_name=settings.CLOUDINARY_CLOUD_NAME,
        api_key=settings.CLOUDINARY_API_KEY,
        api_secret=settings.CLOUDINARY_API_SECRET,
        secure=True
    )


async def upload_file(file: UploadFile, folder: str = "medrem/reports") -> dict:
    """
    Upload a file to Cloudinary.

    Args:
        file: The uploaded file
        folder: Cloudinary folder path

    Returns:
        dict with 'secure_url' and 'public_id'
    """
    init_cloudinary()

    # Read file contents
    contents = await file.read()

    # Determine resource type
    content_type = file.content_type or ""
    if content_type.startswith("image/"):
        resource_type = "image"
    else:
        resource_type = "raw"

    # Upload to Cloudinary
    result = cloudinary.uploader.upload(
        contents,
        folder=folder,
        resource_type=resource_type,
        use_filename=True,
        unique_filename=True,
    )

    return {
        "secure_url": result["secure_url"],
        "public_id": result["public_id"],
        "format": result.get("format", ""),
        "resource_type": result.get("resource_type", ""),
    }


async def delete_file(public_id: str, resource_type: str = "image") -> bool:
    """
    Delete a file from Cloudinary.

    Args:
        public_id: The Cloudinary public ID
        resource_type: "image" or "raw"

    Returns:
        True if deleted successfully
    """
    init_cloudinary()

    result = cloudinary.uploader.destroy(public_id, resource_type=resource_type)
    return result.get("result") == "ok"
