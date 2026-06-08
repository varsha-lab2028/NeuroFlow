import tkinter as tk
from tkinter import ttk
import serial
import threading
import numpy as np
from tensorflow.keras.models import load_model
import time
from collections import deque
import json

# Settings
SERIAL_PORT = "COM16"  # Change to your port
BAUD_RATE = 115200

SEQ_LEN = 50
N_FEATURES = 6

# Detection thresholds
START_THRESHOLD = 25
STOP_THRESHOLD = 8
STOP_TIME = 1.2
MIN_GESTURE_LEN = 30
MAX_GESTURE_LEN = 150

CONF_THRESHOLD = 70

# Filter settings
FILTER_WINDOW = 5

# Load model and parameters
try:
    model = load_model("digit_model.h5")
    print("✅ Model loaded successfully")
except Exception as e:
    print(f"❌ Error loading model: {e}")
    print("Please train the model first using train_digits_model.py")
    exit(1)

try:
    with open("digit_norm_params.json", "r") as f:
        norm_params = json.load(f)
        labels = norm_params['labels']
        NORM_MEAN = np.array(norm_params['mean'])
        NORM_STD = np.array(norm_params['std'])
    print(f"✅ Labels loaded: {labels}")
except Exception as e:
    print(f"❌ Error loading parameters: {e}")
    exit(1)

# Connect to Arduino
try:
    ser = serial.Serial(SERIAL_PORT, BAUD_RATE, timeout=1)
    time.sleep(2)
    print(f"✅ Connected to {SERIAL_PORT}")
except Exception as e:
    print(f"❌ Could not connect to {SERIAL_PORT}: {e}")
    print("Please check if Arduino is connected and port is correct")
    exit(1)

# Globals
recording = False
stop_start = None
captured_data = []
recording_buffer = []

# GUI Setup
root = tk.Tk()
root.title("✍️ Air Writing Digit Recognition")
root.geometry("900x650")
root.configure(bg='#1a1a2e')

# Colors
BG_COLOR = '#1a1a2e'
SECONDARY_COLOR = '#16213e'
ACCENT_COLOR = '#e94560'
TEXT_COLOR = '#c4d0e3'
FRAME_COLOR = '#0f3460'

# Main frame
main_frame = tk.Frame(root, bg=BG_COLOR)
main_frame.pack(expand=True, fill='both', padx=20, pady=20)

# Title
title_label = tk.Label(
    main_frame,
    text="✍️ AIR WRITING DIGIT RECOGNITION",
    font=("Segoe UI", 24, "bold"),
    bg=BG_COLOR,
    fg=ACCENT_COLOR
)
title_label.pack(pady=20)

# Subtitle
subtitle_label = tk.Label(
    main_frame,
    text="Write digits 0-9 in the air",
    font=("Segoe UI", 14),
    bg=BG_COLOR,
    fg=TEXT_COLOR
)
subtitle_label.pack(pady=(0, 30))

# Digit display frame
digit_frame = tk.Frame(
    main_frame, 
    bg=FRAME_COLOR, 
    relief='ridge', 
    bd=3,
    height=200
)
digit_frame.pack(pady=20, padx=50, fill='x')
digit_frame.pack_propagate(False)

digit_var = tk.StringVar()
digit_var.set("?")

digit_label = tk.Label(
    digit_frame,
    textvariable=digit_var,
    font=("Segoe UI", 100, "bold"),
    bg=FRAME_COLOR,
    fg=ACCENT_COLOR
)
digit_label.pack(expand=True, fill='both')

# Confidence frame
conf_frame = tk.Frame(main_frame, bg=BG_COLOR)
conf_frame.pack(pady=20, fill='x', padx=50)

confidence_var = tk.StringVar()
confidence_var.set("Confidence: 0%")

conf_label = tk.Label(
    conf_frame,
    textvariable=confidence_var,
    font=("Segoe UI", 14),
    bg=BG_COLOR,
    fg=TEXT_COLOR
)
conf_label.pack()

# Progress bar
conf_progress = ttk.Progressbar(
    conf_frame,
    length=500,
    mode='determinate'
)
conf_progress.pack(pady=10)

# Status frame
status_frame = tk.Frame(main_frame, bg=FRAME_COLOR, relief='sunken', bd=2)
status_frame.pack(pady=20, fill='x', padx=50)

status_var = tk.StringVar()
status_var.set("🤚 READY - Write a digit 0-9 in the air")

status_label = tk.Label(
    status_frame,
    textvariable=status_var,
    font=("Segoe UI", 14),
    bg=FRAME_COLOR,
    fg=TEXT_COLOR,
    pady=15
)
status_label.pack()

# Info frame
info_frame = tk.Frame(main_frame, bg=BG_COLOR)
info_frame.pack(pady=20)

samples_var = tk.StringVar()
samples_var.set("Samples: 0")

samples_label = tk.Label(
    info_frame,
    textvariable=samples_var,
    font=("Segoe UI", 11),
    bg=BG_COLOR,
    fg=TEXT_COLOR
)
samples_label.pack()

# Motion indicator frame
motion_frame = tk.Frame(main_frame, bg=SECONDARY_COLOR)
motion_frame.pack(pady=10, padx=50, fill='x')

motion_label = tk.Label(
    motion_frame,
    text="Motion Intensity:",
    font=("Segoe UI", 10),
    bg=SECONDARY_COLOR,
    fg=TEXT_COLOR
)
motion_label.pack(pady=(5, 0))

motion_indicator = tk.Canvas(
    motion_frame, 
    width=400, 
    height=15, 
    bg=SECONDARY_COLOR,
    highlightthickness=0
)
motion_indicator.pack(pady=5, padx=10, fill='x')

# Instructions
instruction_text = """💡 Instructions:
• Write digits 0-9 clearly in the air
• Hold still for 1 second when finished
• The system will recognize the digit automatically
• For best results, write at a moderate, consistent speed
• LED will glow when 'NONE' is detected"""

instruction_label = tk.Label(
    main_frame,
    text=instruction_text,
    font=("Segoe UI", 10),
    bg=BG_COLOR,
    fg=TEXT_COLOR,
    justify='left'
)
instruction_label.pack(pady=20)

# Signal processing functions
def moving_average(data, window_size):
    if len(data) < window_size:
        return data
    return np.convolve(data, np.ones(window_size)/window_size, mode='same')

def calculate_motion_intensity(values):
    ax, ay, az, gx, gy, gz = values
    acc_mag = np.sqrt(ax**2 + ay**2 + az**2)
    gyro_mag = np.sqrt(gx**2 + gy**2 + gz**2)
    return acc_mag + gyro_mag * 0.3

def preprocess_gesture(data):
    if len(data) < MIN_GESTURE_LEN:
        return None
    
    data = np.array(data)
    
    # Apply smoothing
    smoothed = np.zeros_like(data)
    for i in range(data.shape[1]):
        smoothed[:, i] = moving_average(data[:, i], FILTER_WINDOW)
    
    # Normalize
    normalized = (smoothed - NORM_MEAN) / NORM_STD
    
    # Handle sequence length
    if len(normalized) < SEQ_LEN:
        pad_len = SEQ_LEN - len(normalized)
        padded = np.vstack([normalized, np.zeros((pad_len, N_FEATURES))])
        return padded
    elif len(normalized) > SEQ_LEN:
        return normalized[:SEQ_LEN]
    else:
        return normalized

# Function to send LED trigger command to Arduino
def trigger_arduino_led():
    """Send command to Arduino to turn on LED for NONE detection"""
    try:
        # Send a special command to Arduino
        ser.write(b"LED_ON\n")
        print("💡 LED triggered for NONE detection")
    except Exception as e:
        print(f"Failed to send LED command: {e}")

# GUI update functions
def gui_prediction(digit, confidence):
    root.after(0, lambda d=digit, c=confidence: digit_var.set(d))
    root.after(0, lambda c=confidence: confidence_var.set(f"Confidence: {c:.1f}%"))
    root.after(0, lambda c=confidence: conf_progress.configure(value=c))

def gui_status(text):
    root.after(0, lambda t=text: status_var.set(t))

def gui_samples(count):
    root.after(0, lambda c=count: samples_var.set(f"Samples: {c}"))

def gui_motion_intensity(intensity):
    """Update motion indicator"""
    normalized = min(int(intensity * 4), 400)
    root.after(0, lambda: motion_indicator.delete("all"))
    root.after(0, lambda n=normalized: motion_indicator.create_rectangle(
        0, 0, n, 15, 
        fill=ACCENT_COLOR, 
        outline=''
    ))

def reset_display():
    root.after(0, lambda: digit_var.set("?"))
    root.after(0, lambda: status_var.set("🤚 Ready - Write a digit 0-9 in the air"))
    root.after(0, lambda: confidence_var.set("Confidence: 0%"))
    root.after(0, lambda: conf_progress.configure(value=0))

def run_prediction(data_buffer):
    if len(data_buffer) < MIN_GESTURE_LEN:
        gui_status("⚠️ Gesture too short! Try again")
        root.after(2000, reset_display)
        return
    
    gui_status("🧠 Recognizing digit...")
    
    processed_data = preprocess_gesture(data_buffer)
    
    if processed_data is None:
        gui_status("❌ Invalid gesture data")
        root.after(2000, reset_display)
        return
    
    input_data = processed_data.reshape(1, SEQ_LEN, N_FEATURES)
    
    try:
        predictions = model.predict(input_data, verbose=0)[0]
        top_idx = np.argmax(predictions)
        confidence = predictions[top_idx] * 100
        predicted_digit = labels[top_idx]
        
        gui_prediction(predicted_digit, confidence)
        
        if predicted_digit == "NONE":
            gui_status(f"⏸️ No digit detected - Try writing more clearly")
            # Trigger LED on Arduino when NONE is detected
            trigger_arduino_led()
        elif confidence < CONF_THRESHOLD:
            gui_status(f"❓ Low confidence ({confidence:.1f}%) - Try writing more clearly")
        else:
            gui_status(f"✓ Recognized digit: {predicted_digit} (Confidence: {confidence:.1f}%)")
        
        root.after(3000, reset_display)
        
    except Exception as e:
        print(f"Prediction error: {e}")
        gui_status("❌ Prediction error occurred")
        root.after(2000, reset_display)

def read_serial():
    global recording, stop_start, captured_data, recording_buffer
    
    motion_buffer = deque(maxlen=20)
    
    while True:
        try:
            # Check for any incoming commands from Python to Arduino
            # This is for bidirectional communication if needed
            
            line = ser.readline().decode(errors='ignore').strip()
            
            if not line:
                continue
            
            parts = line.split(",")
            
            if len(parts) != 6:
                continue
            
            try:
                values = list(map(float, parts))
            except ValueError:
                continue
            
            motion = calculate_motion_intensity(values)
            motion_buffer.append(motion)
            avg_motion = np.mean(motion_buffer)
            
            # Update motion indicator
            gui_motion_intensity(avg_motion)
            
            if not recording:
                if avg_motion > START_THRESHOLD:
                    recording = True
                    captured_data = []
                    stop_start = None
                    gui_status("✍️ Writing digit...")
                    gui_samples(0)
                    
                    if recording_buffer:
                        captured_data.extend(recording_buffer[-15:])
                
                recording_buffer.append(values)
                if len(recording_buffer) > MAX_GESTURE_LEN:
                    recording_buffer = recording_buffer[-MAX_GESTURE_LEN:]
            
            else:
                captured_data.append(values)
                gui_samples(len(captured_data))
                
                if avg_motion > STOP_THRESHOLD:
                    stop_start = None
                else:
                    if stop_start is None:
                        stop_start = time.time()
                    elif time.time() - stop_start > STOP_TIME:
                        if len(captured_data) >= MIN_GESTURE_LEN:
                            recording = False
                            gui_status("🎯 Recognizing...")
                            
                            prediction_thread = threading.Thread(
                                target=run_prediction,
                                args=(captured_data.copy(),)
                            )
                            prediction_thread.daemon = True
                            prediction_thread.start()
                        else:
                            recording = False
                            gui_status("📏 Digit too short, try again")
                            root.after(1500, reset_display)
                        
                        captured_data = []
                        stop_start = None
                
                if len(captured_data) > MAX_GESTURE_LEN:
                    recording = False
                    gui_status("⚠️ Gesture too long, try again")
                    root.after(1500, reset_display)
                    captured_data = []
                    stop_start = None
        
        except serial.SerialException as e:
            print(f"Serial error: {e}")
            gui_status("❌ Serial connection lost")
            time.sleep(1)
        except Exception as e:
            print(f"Error: {e}")
            time.sleep(0.1)

# Serial communication thread for reading
serial_thread = threading.Thread(target=read_serial, daemon=True)
serial_thread.start()

# Separate thread for sending commands to Arduino (if needed)
def send_commands():
    """Thread to handle sending commands to Arduino"""
    while True:
        try:
            # This reads commands from a queue if we implement one
            # For now, we're sending directly from run_prediction
            time.sleep(0.1)
        except:
            pass

command_thread = threading.Thread(target=send_commands, daemon=True)
command_thread.start()

# Display startup message
print("\n" + "="*50)
print("✍️ AIR WRITING DIGIT RECOGNITION SYSTEM")
print("="*50)
print("System Ready!")
print(f"Connected to {SERIAL_PORT} at {BAUD_RATE} baud")
print(f"Model loaded with {len(labels)} classes: {labels}")
print("\nHow to use:")
print("1. Write digits 0-9 clearly in the air")
print("2. Hold still for 1 second when finished")
print("3. Watch the screen for recognition results")
print("4. LED will glow for 2 seconds when 'NONE' is detected")
print("="*50 + "\n")

# Run GUI
root.mainloop()

# Cleanup
ser.close()
print("\nApplication closed.")
