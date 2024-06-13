import os
import logging
import random
from PIL import Image, ImageDraw, ImageFont

# Konfiguriere das Logging
logging.basicConfig(filename='create_jpg_jpegs.log', level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Globale Variablen für die Verzeichnisse
INPUT_DIRECTORY = "/Users/David/git/tech.cybersword.images/payloads"
OUTPUT_DIRECTORY = "/Volumes/IMAGES_M"

# Funktion zum Lesen des Inhalts aus einer ".payloads"-Datei
def read_payloads_file(payloads_file_path):
    with open(payloads_file_path, 'rb') as file:
        return file.readlines()

from PIL import Image, ImageDraw

# Funktion zum Schreiben der EXIF-Daten in ein Bild und Hinzufügen eines Textes
def write_exif_with_text(image_path, payloads, counter):
    current_payloads = []
    for payload in payloads:
        image = Image.new('RGB', (100, 100), color="white")
        # Ändere die Größe des Bildes auf 400x400 Pixel
        image = image.resize((400, 400))
        # Füge einen Kreis zum Bild hinzu
        draw = ImageDraw.Draw(image)
        circle_color = "red"  # Farbe des Kreises
        circle_center = (170 + random.randint(0, 30), 170 + random.randint(0, 30))  # Mittelpunkt des Kreises
        circle_radius = random.randint(25, 50)  # Radius des Kreises
        draw.ellipse((circle_center[0] - circle_radius, circle_center[1] - circle_radius,
                      circle_center[0] + circle_radius, circle_center[1] + circle_radius),
                     fill=circle_color, outline=circle_color)
        # Füge einen Text zum Bild hinzu
        text = "cybersword.tech"  # Hier deinen gewünschten Text einfügen
        text_color = "green"  # Farbe des Textes
        text_position = (100, 100)  # Position des Textes
        font = ImageFont.load_default()  # Standard-Schriftart
        draw.text(text_position, text, fill=text_color, font=font)  # Position, Text und Farbe des Textes anpassen
        
        exif_data = image.info.get("exif", b"")
        
        current_payloads.append(payload)
        
        exif_data += b"P: " + b"".join(current_payloads)
        
        output_image_path = os.path.join(OUTPUT_DIRECTORY, f"image_{counter}.jpg")
        image.save(output_image_path, exif=exif_data)
        logging.info(f"Bild mit EXIF-Daten, Text und Kreis erstellt: {output_image_path}")
        counter += 1

# Iteriere durch jede Datei im Verzeichnis
for filename in os.listdir(INPUT_DIRECTORY):
    if filename.endswith(".payloads"):
        payloads_file_path = os.path.join(INPUT_DIRECTORY, filename)
        payloads = read_payloads_file(payloads_file_path)
        
        # Zähler für die Dateinamen
        counter = 0
        max_payloads_size = 65535 * 4

        # Teile die Payloads in Teilen und verarbeite sie
        while payloads:
            try:
                # Erstelle den Dateinamen für das JPEG-Bild
                image_path = os.path.splitext(payloads_file_path)[0] + f"_{counter}.jpg"
                
                # Erzeuge ein leeres Bild
                Image.new('RGB', (100, 100), color="black").save(image_path)
                
                # Schreibe die EXIF-Daten und füge den Text zum Bild hinzu
                write_exif_with_text(image_path, payloads, counter)
                
                # Erhöhe den Zähler für die Dateinamen
                counter += 1
                
                # Entferne die verarbeiteten Payloads
                payloads = payloads[max_payloads_size:]
            except Exception as e:
                logging.error(f"Fehler beim Verarbeiten der Datei {filename}: {e}")
