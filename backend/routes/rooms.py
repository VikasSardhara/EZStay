import os
from flask import Blueprint, request, jsonify
import pandas as pd

rooms_bp = Blueprint("rooms", __name__)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
ROOMS_FILE = os.path.join(BASE_DIR, "../data/rooms.csv")

@rooms_bp.route("/rooms", methods=["GET"])
def get_filtered_rooms():
    """Fetch rooms with optional filters: size, type, guest count"""
    try:
        if not os.path.exists(ROOMS_FILE):
            return jsonify({"error": "rooms.csv not found"}), 404

        df = pd.read_csv(ROOMS_FILE)

        # Normalize column names
        df.rename(columns={"size": "size", "type": "type"}, inplace=True)

        # Get filters
        size = request.args.get("size")  # e.g., King, Queen
        smoking = request.args.get("type")  # Smoking or Non-Smoking
        guests = request.args.get("guests", type=int)

        # Apply filters
        if size:
            df = df[df["size"].str.lower() == size.lower()]
        if smoking:
            df = df[df["type"].str.lower() == smoking.lower()]
        if guests:
            # For now, assume King allows up to 2 guests, Queen up to 4
            def supports_guests(row):
                if row["size"].lower() == "king":
                    return guests <= 2
                elif row["size"].lower() == "queen":
                    return guests <= 4
                return True  # fallback

            df = df[df.apply(supports_guests, axis=1)]

        return jsonify({"rooms": df.to_dict(orient="records")})

    except Exception as e:
        return jsonify({"error": str(e)}), 500
