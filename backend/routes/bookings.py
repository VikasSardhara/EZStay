import os
import csv
import pandas as pd
from flask import Blueprint, request, jsonify
import datetime

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
    """API to book a room with availability check and room validation"""
    try:
        data = request.json
        user_id = data.get("user_id")
        room_id = data.get("room_id")
        check_in_date = data.get("check_in_date")
        check_out_date = data.get("check_out_date")
        num_guests = data.get("num_guests")

        # Validate input fields
        if not all([user_id, room_id, check_in_date, check_out_date, num_guests]):
            return jsonify({"error": "Missing required fields"}), 400

        # Load rooms.csv to validate room_id
        ROOMS_FILE = os.path.join(BASE_DIR, "../data/rooms.csv")
        if not os.path.exists(ROOMS_FILE):
            return jsonify({"error": "Rooms data not found"}), 500

        rooms_df = pd.read_csv(ROOMS_FILE)

        # Ensure room_id exists in rooms.csv
        if room_id not in rooms_df["room_id"].values:
            return jsonify({"error": f"Room ID {room_id} does not exist"}), 400

        # Read existing bookings
        df = pd.read_csv(BOOKINGS_FILE)

        # Check if the room is already booked for the selected dates
        overlapping_bookings = df[
            (df["room_id"] == room_id) &
            ((df["check_in_date"] <= check_out_date) & (df["check_out_date"] >= check_in_date))
        ]

        if not overlapping_bookings.empty:
            return jsonify({"error": "Room already booked for selected dates"}), 400

        # Generate new booking ID
        new_booking_id = 1 if df.empty else int(df["booking_id"].max()) + 1

        # Create new booking entry
        new_booking = {
            "booking_id": new_booking_id,
            "user_id": user_id,
            "room_id": room_id,
            "check_in_date": check_in_date,
            "check_out_date": check_out_date,
            "num_guests": num_guests
        }

        # Append booking to CSV
        with open(BOOKINGS_FILE, "a", newline="") as file:
            writer = csv.DictWriter(file, fieldnames=df.columns)
            if df.empty:
                writer.writeheader()
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

@bookings_bp.route("/bookings/<int:user_id>", methods=["GET"])
def get_user_bookings(user_id):
    """API to fetch all bookings for a specific user"""
    try:
        df = pd.read_csv(BOOKINGS_FILE)

        # Convert user_id column to integers for accurate filtering
        df["user_id"] = df["user_id"].astype(int)

        # Filter bookings by user_id (Fixed)
        user_bookings = df[df["user_id"] == user_id]

        if user_bookings.empty:
            return jsonify({"message": f"No bookings found for user {user_id}"}), 404

        return jsonify({"user_bookings": user_bookings.to_dict(orient="records")})

    except FileNotFoundError:
        return jsonify({"error": "bookings.csv not found"}), 404
    except ValueError:
        return jsonify({"error": "Invalid user_id format"}), 400
