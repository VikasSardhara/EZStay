import os
from flask import Blueprint, jsonify
import pandas as pd

rooms_bp = Blueprint("rooms", __name__)

# Get absolute path of the CSV file
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
ROOMS_FILE = os.path.join(BASE_DIR, "../data/rooms.csv")

@rooms_bp.route("/rooms", methods=["GET"])
def get_rooms():
    """Fetch all available rooms from CSV"""
    try:
        rooms = pd.read_csv(ROOMS_FILE).to_dict(orient="records")
        return jsonify({"rooms": rooms})
    except FileNotFoundError:
        return jsonify({"error": "rooms.csv not found"}), 404
