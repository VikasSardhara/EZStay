import subprocess
import os

# Define the path where your scripts are located
script_dir = os.path.join(os.getcwd(), "backend")

# Define the scripts with full paths
scripts = [
    ["python", os.path.join(script_dir, "app.py")],
    ["python", os.path.join(script_dir, "EZStripe.py")],
    ["python", os.path.join(script_dir, "webhook.py")]
]

# Start all scripts in parallel
processes = [subprocess.Popen(script) for script in scripts]

print("🚀 All backend services are running. Press Ctrl+C to stop them.")

try:
    for p in processes:
        p.wait()
except KeyboardInterrupt:
    print("\nStopping all scripts...")
    for p in processes:
        p.terminate()
