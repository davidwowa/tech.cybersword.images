import os
import logging
from PIL import Image
from PIL.ExifTags import TAGS

# Konfiguriere das Logging
logging.basicConfig(filename='exif.log', level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def read_exif(image_path):
    try:
        # Öffne das Bild und lade die EXIF-Daten
        with Image.open(image_path) as img:
            exif_data = img._getexif()
            if exif_data:
                # Konvertiere die EXIF-Tags in lesbare Namen
                exif_info = {TAGS.get(tag, tag): value for tag, value in exif_data.items()}
                # Schreibe die EXIF-Informationen in das Log
                logging.info("EXIF data for image {}: {}".format(image_path, exif_info))
            else:
                logging.warning("No EXIF data found for image: {}".format(image_path))
    except Exception as e:
        logging.error("Error reading EXIF data for image {}: {}".format(image_path, e))

# Verzeichnis mit den Bildern
image_directory = "/Users/David/sandbox/pics/"

# Iteriere durch jede Datei im Verzeichnis
for filename in os.listdir(image_directory):
    # Überprüfe, ob es sich um eine Bilddatei handelt
    if filename.lower().endswith((".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".webp")):
        image_path = os.path.join(image_directory, filename)
        # Rufe die Funktion auf, um die EXIF-Daten zu lesen und in die Log-Datei zu schreiben
        read_exif(image_path)
