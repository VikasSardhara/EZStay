import os
from flask import Blueprint, request, jsonify
import pandas as pd
from datetime import datetime

rooms_bp = Blueprint("rooms", __name__)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
ROOMS_FILE = os.path.join(BASE_DIR, "../data/rooms.csv")
LOCKS_FILE = os.path.join(BASE_DIR, "../data/cart_locks.csv")

def dates_overlap(start1, end1, start2, end2):
    return start1 < end2 and start2 < end1

def room_locked(room_id, check_in, check_out):
    if not os.path.exists(LOCKS_FILE):
        return False

    locks_df = pd.read_csv(LOCKS_FILE)
    for _, row in locks_df.iterrows():
        if int(row["room_id"]) == int(room_id):
            locked_start = datetime.strptime(row["check_in"], "%Y-%m-%d").date()
            locked_end = datetime.strptime(row["check_out"], "%Y-%m-%d").date()
            if dates_overlap(check_in, check_out, locked_start, locked_end):
                return True
    return False

@rooms_bp.route("/rooms", methods=["GET"])
def get_filtered_rooms():
    try:
        if not os.path.exists(ROOMS_FILE):
            return jsonify({"error": "rooms.csv not found"}), 404

        df = pd.read_csv(ROOMS_FILE)

        size = request.args.get("size")
        smoking = request.args.get("type")
        guests = request.args.get("guests", type=int)
        check_in_str = request.args.get("check_in")
        check_out_str = request.args.get("check_out")

        check_in = datetime.strptime(check_in_str, "%Y-%m-%d").date() if check_in_str else None
        check_out = datetime.strptime(check_out_str, "%Y-%m-%d").date() if check_out_str else None

        if size:
            df = df[df["size"].str.lower() == size.lower()]
        if smoking:
            df = df[df["type"].str.lower() == smoking.lower()]
        if guests:
            def supports_guests(row):
                if row["size"].lower() == "king":
                    return guests <= 2
                elif row["size"].lower() == "queen":
                    return guests <= 4
                return True
            df = df[df.apply(supports_guests, axis=1)]

        # Exclude locked rooms
        if check_in and check_out:
            df = df[~df["room_id"].apply(lambda rid: room_locked(rid, check_in, check_out))]

        return jsonify({"rooms": df.to_dict(orient="records")})

    except Exception as e:
        return jsonify({"error": str(e)}), 500
