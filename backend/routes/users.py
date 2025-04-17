import os
import csv
import pandas as pd
from flask import Blueprint, request, jsonify
from datetime import date

users_bp = Blueprint("users", __name__)
# Absolute path for CSV file
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
USERS_FILE = os.path.join(BASE_DIR, "../data/users.csv")

# Ensure users.csv exists with proper headers
if not os.path.exists(USERS_FILE):
    df = pd.DataFrame(columns=["user_id", "name", "email", "dob"])
    df.to_csv(USERS_FILE, index=False)

@users_bp.route("/register", methods=["POST"])
def register_user():
    """API to register a new user"""
    try:
        data = request.json
        name = data.get("name")
        email = data.get("email")
        dob = data.get("dob")

        # Validate input fields
        if not all([name, email, dob]):
            return jsonify({"error": "Missing required fields"}), 400

        # Check if the file exists and is not empty
        if os.stat(USERS_FILE).st_size == 0:
            df = pd.DataFrame(columns=["user_id", "name", "email", "dob", "creation_date"])
        else:
            df = pd.read_csv(USERS_FILE)

        # Check if email already exists
        if not df.empty and email in df["email"].values:
            return jsonify({"error": "Email is already registered"}), 400

        # Generate new user ID
        new_user_id = 1001 if df.empty else int(df["user_id"].max()) + 1    

        #Create new user entry
        new_user = {
            "user_id": new_user_id,
            "name": name,
            "email": email,
            "dob": dob,
            "creation_date": date.today().strftime('%m/%d/%Y')
        }

        # Append user to CSV
        with open(USERS_FILE, "a", newline="") as file:
            writer = csv.DictWriter(file, fieldnames=df.columns)
            if df.empty:
                writer.writeheader()  # Write headers if file is empty
            writer.writerow(new_user)

        return jsonify({"message": "User registered successfully", "user_id": new_user_id}), 201

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@users_bp.route("/users", methods=["GET"])
def get_all_users():
    """API to fetch all users"""
    try:
        df = pd.read_csv(USERS_FILE)
        if df.empty:
            return jsonify({"message": "No users found"}), 404
        return jsonify({"users": df.to_dict(orient="records")})
    except FileNotFoundError:
        return jsonify({"error": "users.csv not found"}), 404
    

@users_bp.route("/users/<string:email>", methods=["GET"])
def get_user_by_email(email):
    """API to fetch user information by email"""
    try:
        df = pd.read_csv(USERS_FILE)

        # Filter users by email (case insensitive)
        user_info = df[df["email"].str.lower() == email.lower()]

        if user_info.empty:
            return jsonify({"message": f"No user found for {email}"}), 404

        return jsonify({"users": user_info.to_dict(orient="records")})

    except FileNotFoundError:
        return jsonify({"error": "users.csv not found"}), 404
    except ValueError:
        return jsonify({"error": "Invalid email format"}), 400
