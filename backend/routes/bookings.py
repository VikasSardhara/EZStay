import os
import csv
import pandas as pd
from flask import Blueprint, request, jsonify

bookings_bp = Blueprint("bookings", __name__)

# Get absolute path of the CSV file (Same as rooms.py)
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
BOOKINGS_FILE = os.path.join(BASE_DIR, "../data/bookings.csv")

# Ensure CSV file exists with proper headers
if not os.path.exists(BOOKINGS_FILE):
    df = pd.DataFrame(columns=["booking_id", "user_id", "room_id", "check_in_date", "check_out_date", "num_guests"])
    df.to_csv(BOOKINGS_FILE, index=False)

@bookings_bp.route("/book", methods=["POST"])  
def book_room():
    """API to book a room"""
    try:
        # Get JSON data from request
        data = request.json
        user_id = data.get("user_id")
        room_id = data.get("room_id")
        check_in_date = data.get("check_in_date")
        check_out_date = data.get("check_out_date")
        num_guests = data.get("num_guests")

        # Validate input
        if not all([user_id, room_id, check_in_date, check_out_date, num_guests]):
            return jsonify({"error": "Missing required fields"}), 400

        # Read existing bookings
        try:
            df = pd.read_csv(BOOKINGS_FILE)
        except FileNotFoundError:
            df = pd.DataFrame(columns=["booking_id", "user_id", "room_id", "check_in_date", "check_out_date", "num_guests"])

        # Generate new booking ID
        new_booking_id = 1 if df.empty else int(df["booking_id"].max()) + 1

        # Create new booking dictionary
        new_booking = {
            "booking_id": new_booking_id,
            "user_id": user_id,
            "room_id": room_id,
            "check_in_date": check_in_date,
            "check_out_date": check_out_date,
            "num_guests": num_guests
        }

        # Append new booking to CSV
        with open(BOOKINGS_FILE, "a", newline="") as file:
            writer = csv.DictWriter(file, fieldnames=df.columns)
            if file.tell() == 0:
                writer.writeheader()  # Write header if the file is new
            writer.writerow(new_booking)

        return jsonify({"message": "Booking successful", "booking_id": new_booking_id}), 201

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@bookings_bp.route("/bookings", methods=["GET"])
def get_all_bookings():
    """API to fetch all bookings"""
    try:
        df = pd.read_csv(BOOKINGS_FILE)
        if df.empty:
            return jsonify({"message": "No bookings found"}), 404
        return jsonify({"bookings": df.to_dict(orient="records")})
    except FileNotFoundError:
        return jsonify({"error": "bookings.csv not found"}), 404
