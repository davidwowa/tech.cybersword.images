import logging
from PIL import Image
from PIL.ExifTags import TAGS
import base64
import os

# Konfiguriere das Logging
logging.basicConfig(filename='change_exif.log', level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def read_exif(image_path):
    image = Image.open(image_path)
    exif_data = image.getexif()
    if exif_data:
        for tag, value in exif_data.items():
            tag_name = TAGS.get(tag, tag)
            logging.info("{}: {}".format(tag_name, value))

def update_exif(image_path, exif_properties):
    try:
        image = Image.open(image_path)
    except Exception as e:
        logging.error("Error opening image {}: {}".format(image_path, e))
        return
    
    # Konvertiere das Bild in den RGB-Modus, falls es im RGBA-Modus vorliegt
    if image.mode == 'RGBA':
        image = image.convert('RGB')
    
    # Überprüfe, ob das Bild EXIF-Daten enthält
    exif_data = image.getexif()
    if exif_data:
        for tag, value in exif_properties.items():
            # Convert property values to proper EXIF format if necessary
            # Here you might need to do some data type conversions based on the EXIF tag requirements
            exif_data[tag] = value
        
        # Dateinamen ändern, um das Präfix "p_" hinzuzufügen
        image_name = os.path.basename(image_path)
        image_name_prefix = "p_" + image_name
        
        # Speichere das ursprüngliche Bild mit aktualisierten EXIF-Daten
        original_image_path = os.path.join(os.path.dirname(image_path), image_name_prefix)
        image.save(original_image_path)
        logging.info("Saved original image with prefix 'p_': {}".format(original_image_path))
        
        # Speichere eine Kopie des Bildes als JPEG mit dem geänderten Dateinamen
        jpeg_path = os.path.join(os.path.dirname(image_path), "p_" + os.path.splitext(image_name)[0] + ".jpg")
        image.save(jpeg_path, quality=50)
        logging.info("Saved JPEG copy with prefix 'p_': {}".format(jpeg_path))
        
        # Speichere eine Kopie des Bildes als JPEG mit dem geänderten Dateinamen
        jpeg_path = os.path.join(os.path.dirname(image_path), "p_" + os.path.splitext(image_name)[0] + ".jpeg")
        image.save(jpeg_path, quality=100)
        logging.info("Saved JPEG copy with prefix 'p_': {}".format(jpeg_path))
        
        logging.info("Updated EXIF data for image: {}".format(image_path))
    else:
        logging.warning("No EXIF data found in image: {}".format(image_path))

# Pfad zur Property-Datei
property_file_path = "/Users/David/sandbox/pics/mapping.properties"

# Lese die EXIF-Property-Datei
exif_properties = {}
with open(property_file_path, 'r') as f:
    count = 0
    for line in f:
        try:
            key, value = line.strip().split('=', maxsplit=1)
            exif_properties[key.strip()] = base64.b64decode(value.strip()).decode()
        except Exception as e:
            count += 1
            logging.error("Error reading property file: {}".format(e))
            logging.error("Skipping line: {}".format(line))
    if count > 0:
        logging.warning("There were {} errors reading the property file.".format(count))

# Verzeichnis, das die Bilder enthält
image_directory = "/Users/David/sandbox/pics/"

# image = Image.open("/Users/David/sandbox/pics/t.jpeg")
# image.save("/Users/David/sandbox/pics/test.jpeg", quality=50)

# Iteriere durch jedes Bild im Verzeichnis
for filename in os.listdir(image_directory):
    if filename.lower().endswith((".gif", ".tif", ".tiff", ".png", ".bmp", ".webp")):
        image_path = os.path.join(image_directory, filename)
        try:
            # Lese die ursprünglichen EXIF-Daten
            logging.info("Original EXIF data for image: {}".format(filename))
            read_exif(image_path)
            # Aktualisiere die EXIF-Daten mit den Eigenschaften aus der Property-Datei
            update_exif(image_path, exif_properties)
            # Zeige die aktualisierten EXIF-Daten an
            logging.info("Updated EXIF data for image: {}".format(filename))
            read_exif(image_path)
            # print("\n")
        except Exception as e:
            logging.error("Error processing image {}: {}".format(image_path, e))