<<<<<<< HEAD
import pytest
from app import app

@pytest.fixture
def client():
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client

def test_home(client):
    response = client.get('/')
    assert response.status_code == 200
    assert b"EZStay Backend API is running!" in response.data

def test_book_room_success(client):
    payload = {
        "user_id": "1",
        "room_id": "101",  # make sure this exists in rooms.csv
        "check_in_date": "2025-05-10",
        "check_out_date": "2025-05-12",
        "num_guests": 2
    }
    response = client.post('/book', json=payload)
    print("Response JSON:", response.json)  # Helpful for debugging
    assert response.status_code == 201  # ✅ expect 201 Created now
    assert b"booking successful" in response.data.lower()  # ✅ match your success message

def test_book_room_missing_field(client):
    payload = {
        "user_id": "1",
        "check_in_date": "2025-05-10",
        "check_out_date": "2025-05-12",
        "num_guests": 2
    }
    response = client.post('/book', json=payload)
    print("Response JSON (missing field):", response.json)  # Debug line
    assert response.status_code == 400  # ✅ explicitly test for bad request
    assert b"missing required fields" in response.data.lower()  # ✅ check error message
=======
import os
import tempfile
import shutil
import pandas as pd
import pytest
from app import app  # your main Flask app should register users_bp
import users  # this imports the users.py where users_bp is defined

@pytest.fixture
def client():
    # Configure the app for testing
    app.config['TESTING'] = True
    
    # Create temp dir and patch USERS_FILE
    temp_dir = tempfile.mkdtemp()
    test_csv_path = os.path.join(temp_dir, "users.csv")
    pd.DataFrame(columns=["user_id", "name", "email", "dob", "creation_date"]).to_csv(test_csv_path, index=False)

    # Patch the path used in users.py
    original_path = users.USERS_FILE
    users.USERS_FILE = test_csv_path

    with app.test_client() as client:
        yield client

    # Clean up
    users.USERS_FILE = original_path
    shutil.rmtree(temp_dir)

def test_register_success(client):
    payload = {
        "name": "Ved Starks",
        "email": "ved@example.com",
        "dob": "2000-01-01"
    }
    response = client.post('/register', json=payload)
    assert response.status_code == 201
    assert b"user registered successfully" in response.data.lower()

def test_register_missing_fields(client):
    payload = {
        "name": "Ved"
        # Missing email and dob
    }
    response = client.post('/register', json=payload)
    assert response.status_code == 400
    assert b"missing required fields" in response.data.lower()

def test_register_duplicate_email(client):
    payload = {
        "name": "Ved",
        "email": "ved@example.com",
        "dob": "2000-01-01"
    }
    client.post('/register', json=payload)  # First registration
    response = client.post('/register', json=payload)  # Duplicate
    assert response.status_code == 400
    assert b"email is already registered" in response.data.lower()

def test_get_all_users_empty(client):
    response = client.get('/users')
    assert response.status_code == 404
    assert b"no users found" in response.data.lower()

def test_get_all_users_after_registration(client):
    payload = {
        "name": "Ved",
        "email": "ved@example.com",
        "dob": "2000-01-01"
    }
    client.post('/register', json=payload)
    response = client.get('/users')
    assert response.status_code == 200
    assert b"ved@example.com" in response.data.lower()

def test_get_user_by_email_found(client):
    payload = {
        "name": "Ved",
        "email": "ved@example.com",
        "dob": "2000-01-01"
    }
    client.post('/register', json=payload)
    response = client.get('/users/ved@example.com')
    assert response.status_code == 200
    assert b"ved@example.com" in response.data.lower()

def test_get_user_by_email_not_found(client):
    response = client.get('/users/missing@example.com')
    assert response.status_code == 404
    assert b"no user found for" in response.data.lower()
>>>>>>> 54e63762880cba51b179e7b9d6c14d38264b3d60
