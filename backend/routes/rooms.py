import os
from flask import Blueprint, request, jsonify
import pandas as pd

rooms_bp = Blueprint("rooms", __name__)

# Get absolute path of the CSV file
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
ROOMS_FILE = os.path.join(BASE_DIR, "../data/rooms.csv")

@rooms_bp.route("/rooms", methods=["GET"])
def get_rooms():
    """Fetch available rooms with optional filters for size and type"""
    try:
        df = pd.read_csv(ROOMS_FILE)

        # Rename columns to match the CSV format
        df.rename(columns={"size": "size", "type": "type"}, inplace=True)

        # Get filter parameters from request
        size = request.args.get("size")  # King or Queen
        room_type = request.args.get("type")  # Smoking or Non-Smoking

        # Apply filters if provided
        if size:
            df = df[df["size"].str.lower() == size.lower()]
        if room_type:
            df = df[df["type"].str.lower() == room_type.lower()]

        return jsonify({"rooms": df.to_dict(orient="records")})

    except FileNotFoundError:
        return jsonify({"error": "rooms.csv not found"}), 404
