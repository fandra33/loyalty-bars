import requests
import json
import sys

BASE_URL = "http://localhost:8081/api"
CLIENT_EMAIL = "mail2@gmail.com"
CLIENT_PASS = "mail1@gmail.com"
ADMIN_EMAIL = "newadmin@bar.com"
ADMIN_PASS = "password123"

def print_step(msg):
    print(f"\n🔹 {msg}")

def login(email, password, role_name):
    print_step(f"Logging in as {role_name} ({email})...")
    resp = requests.post(f"{BASE_URL}/auth/login", json={"email": email, "password": password})
    if resp.status_code != 200:
        print(f"❌ Login failed: {resp.text}")
        sys.exit(1)
    print("✅ Login successful")
    return resp.json()["token"]

def test_client_flow():
    token = login(CLIENT_EMAIL, CLIENT_PASS, "Client")
    headers = {"Authorization": f"Bearer {token}"}

    # 1. Get Profile
    print_step("Getting Client Profile...")
    resp = requests.get(f"{BASE_URL}/auth/me", headers=headers)
    if resp.status_code != 200:
        print(f"❌ Failed to get profile: {resp.text}")
        sys.exit(1)
    profile = resp.json()
    initial_points = profile['pointsBalance']
    print(f"✅ Profile loaded. Current Points: {initial_points}")

    # 2. List Bars
    print_step("Listing Bars...")
    resp = requests.get(f"{BASE_URL}/bars", headers=headers)
    if resp.status_code != 200:
        print(f"❌ Failed to list bars: {resp.text}")
        sys.exit(1)
    bars = resp.json()
    if not bars:
        print("❌ No bars found")
        sys.exit(1)
    bar_id = bars[0]['id']
    print(f"✅ Found {len(bars)} bars. Using Bar ID: {bar_id} ({bars[0]['name']})")

    # 3. List Rewards
    print_step("Listing Rewards...")
    resp = requests.get(f"{BASE_URL}/rewards", headers=headers)
    if resp.status_code != 200:
        print(f"❌ Failed to list rewards: {resp.text}")
    else:
        print(f"✅ Found {len(resp.json())} rewards")

    # 4. Generate QR
    print_step("Generating QR Code for transaction ($50.00)...")
    resp = requests.post(f"{BASE_URL}/qr/generate", 
                         json={"barId": bar_id, "amount": 50.00}, 
                         headers=headers)
    if resp.status_code != 200:
        print(f"❌ Failed to generate QR: {resp.text}")
        sys.exit(1)
    qr_data = resp.json()
    
    if not qr_data.get('qrImageData'):
        print("❌ QR Code successfully generated but 'qrImageData' is MISSING or NULL!")
        print("   This explains why the frontend shows no image.")
        sys.exit(1)

    print(f"✅ QR Code Generated: {qr_data['qrCode']}")
    print(f"✅ QR Image Data received ({len(qr_data['qrImageData'])} chars)")
    
    return qr_data['qrCode'], initial_points

def test_admin_flow(qr_code):
    token = login(ADMIN_EMAIL, ADMIN_PASS, "Bar Admin")
    headers = {"Authorization": f"Bearer {token}"}

    # 1. Validate QR
    print_step(f"Validating QR Code {qr_code}...")
    resp = requests.post(f"{BASE_URL}/qr/validate",
                         json={"qrCode": qr_code},
                         headers=headers)
    if resp.status_code != 200:
        print(f"❌ Failed to validate QR: {resp.text}")
        sys.exit(1)
    transaction = resp.json()
    print(f"✅ Transaction Successful! Points Earned: {transaction.get('pointsEarned', 'N/A')}")

def verify_points(initial_points):
    token = login(CLIENT_EMAIL, CLIENT_PASS, "Client")
    headers = {"Authorization": f"Bearer {token}"}

    print_step("Verifying Points Balance update...")
    resp = requests.get(f"{BASE_URL}/auth/me", headers=headers)
    new_points = resp.json()['pointsBalance']
    
    if new_points > initial_points:
        print(f"✅ Points updated! {initial_points} -> {new_points}")
    else:
        print(f"❌ Points did not increase. {initial_points} -> {new_points}")
        sys.exit(1)

if __name__ == "__main__":
    try:
        qr_code, initial_points = test_client_flow()
        test_admin_flow(qr_code)
        verify_points(initial_points)
        print("\n🎉 ALL TESTS PASSED SUCCESSFULLY!")
    except Exception as e:
        print(f"\n❌ An error occurred: {e}")
        sys.exit(1)
