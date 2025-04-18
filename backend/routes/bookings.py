"""
bookings.py

Handles all room booking actions, including creating, viewing, canceling bookings,
and managing temporary room locks during the cart process.

Author: EZStay Backend Team
Date: April 2025
"""



import os
import csv
import pandas as pd
from flask import Blueprint, request, jsonify
import datetime

bookings_bp = Blueprint("bookings", __name__)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
BOOKINGS_FILE = os.path.join(BASE_DIR, "../data/bookings.csv")
LOCKS_FILE = os.path.join(BASE_DIR, "../data/cart_locks.csv")

# Ensure CSV file exists with proper headers
if not os.path.exists(BOOKINGS_FILE):
    df = pd.DataFrame(columns=["booking_id", "user_id", "room_id", "check_in_date", "check_out_date", "num_guests"])
    df.to_csv(BOOKINGS_FILE, index=False)

@bookings_bp.route("/book", methods=["POST"])  
def book_room():
    """
    Book a room for selected dates.

    Request JSON:
        {
            "user_id": str,
            "room_id": str,
            "check_in_date": "YYYY-MM-DD",
            "check_out_date": "YYYY-MM-DD",
            "num_guests": int
        }

    Returns:
        JSON: Success message with booking ID or error details.
    """


    try:
        data = request.json
        user_id = data.get("user_id")
        room_id = data.get("room_id")
        check_in_date = data.get("check_in_date")
        check_out_date = data.get("check_out_date")
        num_guests = data.get("num_guests")

        if not all([user_id, room_id, check_in_date, check_out_date, num_guests]):
            return jsonify({"error": "Missing required fields"}), 400

        ROOMS_FILE = os.path.join(BASE_DIR, "../data/rooms.csv")
        if not os.path.exists(ROOMS_FILE):
            return jsonify({"error": "Rooms data not found"}), 500

        rooms_df = pd.read_csv(ROOMS_FILE)
        if room_id not in rooms_df["room_id"].values:
            return jsonify({"error": f"Room ID {room_id} does not exist"}), 400

        df = pd.read_csv(BOOKINGS_FILE)
        overlapping_bookings = df[
            (df["room_id"] == room_id) &
            ((df["check_in_date"] <= check_out_date) & (df["check_out_date"] >= check_in_date))
        ]
        if not overlapping_bookings.empty:
            return jsonify({"error": "Room already booked for selected dates"}), 400

        new_booking_id = 1001 if df.empty else int(df["booking_id"].max()) + 1
        new_booking = {
            "booking_id": new_booking_id,
            "user_id": user_id,
            "room_id": room_id,
            "check_in_date": check_in_date,
            "check_out_date": check_out_date,
            "num_guests": num_guests
        }

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
    
    """
    Get a list of all bookings from the CSV.

    Returns:
        JSON list of all bookings or 404 if none exist.
    """

    try:
        df = pd.read_csv(BOOKINGS_FILE)
        if df.empty:
            return jsonify({"message": "No bookings found"}), 404
        return jsonify({"bookings": df.to_dict(orient="records")})
    except FileNotFoundError:
        return jsonify({"error": "bookings.csv not found"}), 404

@bookings_bp.route("/bookings/<int:user_id>", methods=["GET"])
def get_user_bookings(user_id):

    """
    Get bookings made by a specific user.

    Args:
        user_id (int): ID of the user.

    Returns:
        JSON list of user-specific bookings or 404 if not found.
    """


    try:
        df = pd.read_csv(BOOKINGS_FILE)
        df["user_id"] = df["user_id"].astype(int)
        user_bookings = df[df["user_id"] == user_id]

        if user_bookings.empty:
            return jsonify({"message": f"No bookings found for user {user_id}"}), 404

        return jsonify({"user_bookings": user_bookings.to_dict(orient="records")})

    except FileNotFoundError:
        return jsonify({"error": "bookings.csv not found"}), 404
    except ValueError:
        return jsonify({"error": "Invalid user_id format"}), 400

@bookings_bp.route("/bookings/<int:booking_id>", methods=["DELETE"])
def delete_booking(booking_id):

    """
    Cancel and delete a booking by its booking_id.

    Args:
        booking_id (int): ID of the booking to cancel.

    Returns:
        JSON: Success or error message.
    """


    try:
        df = pd.read_csv(BOOKINGS_FILE)
        if booking_id not in df["booking_id"].values:
            return jsonify({"error": f"Booking ID {booking_id} not found"}), 404

        df = df[df["booking_id"] != booking_id]
        df.to_csv(BOOKINGS_FILE, index=False)

        return jsonify({"message": f"Booking {booking_id} cancelled successfully"}), 200

    except FileNotFoundError:
        return jsonify({"error": "bookings.csv not found"}), 404
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@bookings_bp.route("/lock", methods=["POST"])
def lock_cart_room():

    """
    Temporarily lock a room in the cart.

    Request JSON:
        {
            "room_id": str,
            "check_in": "YYYY-MM-DD",
            "check_out": "YYYY-MM-DD"
        }

    Returns:
        JSON message confirming lock or error.
    """


    try:
        data = request.get_json()
        print("Received lock request:", data)

        room_id = data.get("room_id")
        check_in = data.get("check_in")
        check_out = data.get("check_out")

        if not all([room_id, check_in, check_out]):
            return jsonify({"error": "Missing required fields"}), 400

        check_in_date = datetime.datetime.strptime(check_in, "%Y-%m-%d").date()
        check_out_date = datetime.datetime.strptime(check_out, "%Y-%m-%d").date()

        clean_expired_cart_locks()

        file_exists = os.path.isfile(LOCKS_FILE)
        with open(LOCKS_FILE, mode="a", newline="") as file:
            writer = csv.writer(file)
            if not file_exists:
                writer.writerow(["room_id", "check_in", "check_out"])
            writer.writerow([room_id, check_in, check_out])

        return jsonify({"message": "Room temporarily locked in cart."}), 200

    except Exception as e:
        print("Lock failed:", str(e))
        return jsonify({"error": str(e)}), 500

@bookings_bp.route("/lock", methods=["DELETE"])
def unlock_cart_room():

    """
    Unlock a room from the cart by removing the temporary hold.

    Accepts parameters via JSON or query string.

    Returns:
        JSON message confirming unlock or error.
    """


    try:
        # Try to get data from JSON body first
        data = request.get_json(silent=True)
        
        # If no JSON data, try to get from URL parameters
        if data is None:
            room_id = request.args.get("room_id")
            check_in = request.args.get("check_in")
            check_out = request.args.get("check_out")
        else:
            room_id = data.get("room_id")
            check_in = data.get("check_in")
            check_out = data.get("check_out")
        
        # Debug logging
        print(f"DELETE request received - Room: {room_id}, Check-in: {check_in}, Check-out: {check_out}")
        
        if not all([room_id, check_in, check_out]):
            return jsonify({"error": "Missing required fields"}), 400

        if not os.path.exists(LOCKS_FILE):
            return jsonify({"message": "No locks found"}), 404

        # Read the current locks file
        df = pd.read_csv(LOCKS_FILE)
        # Convert all columns to string for comparison
        df["room_id"] = df["room_id"].astype(str)
        df["check_in"] = df["check_in"].astype(str)
        df["check_out"] = df["check_out"].astype(str)
        
        print(f"Before filtering: {len(df)} rows")
        
        # Count rows before filtering
        original_len = len(df)
        
        # Filter out the row that matches the criteria
        df = df[~(
            (df["room_id"] == str(room_id)) &
            (df["check_in"] == check_in) &
            (df["check_out"] == check_out)
        )]
        
        print(f"After filtering: {len(df)} rows")
        
        # Save the updated dataframe back to CSV
        df.to_csv(LOCKS_FILE, index=False)

        # Check if any row was removed
        removed = original_len - len(df)
        if removed > 0:
            return jsonify({"message": f"Room lock removed ({removed} entries)"}), 200
        else:
            return jsonify({"message": "No matching lock found", 
                           "details": {"room_id": room_id, "check_in": check_in, "check_out": check_out}}), 404

    except Exception as e:
        print(f"Unlock failed: {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500

def clean_expired_cart_locks():

    """
    Removes expired room locks from cart_locks.csv.

    Any lock where the check-out date is before today will be deleted.
    It does not have much significant role but it is safeside file
    """


    try:
        if not os.path.exists(LOCKS_FILE):
            return

        today = datetime.date.today()
        df = pd.read_csv(LOCKS_FILE)
        df["check_out"] = pd.to_datetime(df["check_out"]).dt.date
        df = df[df["check_out"] >= today]
        df.to_csv(LOCKS_FILE, index=False)
    except Exception as e:
        print("Error cleaning expired locks:", str(e))