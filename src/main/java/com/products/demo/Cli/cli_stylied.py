import click
import requests
import json
from tabulate import tabulate
from datetime import datetime

# Base URL for your Spring Boot API
BASE_URL = "http://localhost:8080"

# Global token variable
TOKEN = None

# Color constants for consistent styling
class Colors:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    CYAN = '\033[96m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'
    END = '\033[0m'

# Helper function to handle HTTP requests
def make_request(method, endpoint, data=None, params=None, headers=None):
    url = f"{BASE_URL}{endpoint}"
    default_headers = {"Content-Type": "application/json"}
    if TOKEN and endpoint not in ["/api/auth/register", "/api/auth/login"]:
        default_headers["Authorization"] = f"Bearer {TOKEN}"
    if headers:
        default_headers.update(headers)
    try:
        if method == "GET":
            response = requests.get(url, params=params, headers=default_headers)
        elif method == "POST":
            response = requests.post(url, json=data, headers=default_headers)
        elif method == "PUT":
            response = requests.put(url, json=data, headers=default_headers)
        elif method == "DELETE":
            response = requests.delete(url, headers=default_headers)
        response.raise_for_status()
        return response.json() if response.content else None
    except requests.exceptions.HTTPError as e:
        error_message = e.response.text
        try:
            error_json = e.response.json()
            error_message = error_json.get("message", error_message)
        except ValueError:
            pass
        raise click.ClickException(f"{Colors.RED}Error {e.response.status_code} on {endpoint}: {error_message}{Colors.END}")
    except requests.exceptions.RequestException as e:
        raise click.ClickException(f"{Colors.RED}Request failed to {endpoint}: {str(e)}{Colors.END}")

# Welcome Menu
def show_welcome_menu():
    """Display the initial welcome menu."""
    while True:
        click.echo(f"\n{Colors.CYAN}{Colors.BOLD}╔══════════════════════════════════════════════╗{Colors.END}")
        click.echo(f"{Colors.CYAN}{Colors.BOLD}║           PRODUCT MANAGEMENT CLI            ║{Colors.END}")
        click.echo(f"{Colors.CYAN}{Colors.BOLD}╚══════════════════════════════════════════════╝{Colors.END}")
        click.echo(f"{Colors.YELLOW}1.{Colors.END} {Colors.GREEN}Sign Up{Colors.END}")
        click.echo(f"{Colors.YELLOW}2.{Colors.END} {Colors.GREEN}Sign In{Colors.END}")
        click.echo(f"{Colors.YELLOW}3.{Colors.END} {Colors.RED}Exit{Colors.END}")
        
        choice = click.prompt(f"\n{Colors.BLUE}Enter your choice (1-3){Colors.END}", type=int)

        try:
            if choice == 1:
                signup()
            elif choice == 2:
                signin()
                show_main_menu()
            elif choice == 3:
                click.echo(f"{Colors.YELLOW}Exiting... Thank you for using Product Management CLI!{Colors.END}")
                break
            else:
                click.echo(f"{Colors.RED}Invalid choice. Please select 1-3.{Colors.END}")
        except click.ClickException as e:
            click.echo(f"{Colors.RED}{str(e)}{Colors.END}")
        except Exception as e:
            click.echo(f"{Colors.RED}Unexpected error: {str(e)}{Colors.END}")

# Main Menu (after signin)
def show_main_menu():
    """Display the main menu for product and user management."""
    while True:
        click.echo(f"\n{Colors.CYAN}{Colors.BOLD}╔══════════════════════════════════════════════╗{Colors.END}")
        click.echo(f"{Colors.CYAN}{Colors.BOLD}║                   MAIN MENU                  ║{Colors.END}")
        click.echo(f"{Colors.CYAN}{Colors.BOLD}╚══════════════════════════════════════════════╝{Colors.END}")
        click.echo(f"{Colors.YELLOW}1.{Colors.END} {Colors.GREEN}📦 Manage Products{Colors.END}")
        click.echo(f"{Colors.YELLOW}2.{Colors.END} {Colors.GREEN}👥 Manage Users{Colors.END}")
        click.echo(f"{Colors.YELLOW}3.{Colors.END} {Colors.BLUE}← Return to welcome menu{Colors.END}")
        
        choice = click.prompt(f"\n{Colors.BLUE}Enter your choice (1-3){Colors.END}", type=int)

        try:
            if choice == 1:
                show_product_menu()
            elif choice == 2:
                show_user_menu()
            elif choice == 3:
                click.echo(f"{Colors.YELLOW}Returning to welcome menu...{Colors.END}")
                break
            else:
                click.echo(f"{Colors.RED}Invalid choice. Please select 1-3.{Colors.END}")
        except click.ClickException as e:
            click.echo(f"{Colors.RED}{str(e)}{Colors.END}")
        except Exception as e:
            click.echo(f"{Colors.RED}Unexpected error: {str(e)}{Colors.END}")

# Auth Commands
def signup():
    """Sign up a new user."""
    click.echo(f"\n{Colors.GREEN}{Colors.BOLD}🎉 Create New Account{Colors.END}")
    name = click.prompt(f"{Colors.BLUE}Enter name{Colors.END}")
    email = click.prompt(f"{Colors.BLUE}Enter email{Colors.END}")
    password = click.prompt(f"{Colors.BLUE}Enter password{Colors.END}", hide_input=True)
    age = click.prompt(f"{Colors.BLUE}Enter age{Colors.END}", type=int)
    
    signup_data = {"name": name, "email": email, "password": password, "age": age}
    try:
        data = make_request("POST", "/api/auth/register", data=signup_data)
        click.echo(f"{Colors.GREEN}✅ {data['message']}{Colors.END}")
        if data and "token" in data and data["token"]:
            global TOKEN
            TOKEN = data["token"]
            click.echo(f"{Colors.GREEN}🔑 Token received. Proceeding to main menu...{Colors.END}")
            show_main_menu()
    except click.ClickException as e:
        if "403" in str(e):
            click.echo(f"{Colors.YELLOW}⚠️  Signup may require admin privileges.{Colors.END}")
            if click.confirm(f"{Colors.BLUE}Use admin token for signup?{Colors.END}"):
                admin_token = click.prompt(f"{Colors.BLUE}Enter admin JWT token{Colors.END}", hide_input=True)
                headers = {"Authorization": f"Bearer {admin_token}"}
                try:
                    data = make_request("POST", "/api/auth/register", data=signup_data, headers=headers)
                    click.echo(f"{Colors.GREEN}✅ {data['message']}{Colors.END}")
                except click.ClickException as e:
                    click.echo(f"{Colors.RED}❌ Admin signup failed: {str(e)}{Colors.END}")
        else:
            click.echo(f"{Colors.RED}❌ Sign up failed: {str(e)}{Colors.END}")

def signin():
    """Sign in to get JWT token."""
    global TOKEN
    click.echo(f"\n{Colors.GREEN}{Colors.BOLD}🔐 Sign In{Colors.END}")
    email = click.prompt(f"{Colors.BLUE}Enter email{Colors.END}")
    password = click.prompt(f"{Colors.BLUE}Enter password{Colors.END}", hide_input=True)
    
    signin_data = {"email": email, "password": password}
    try:
        data = make_request("POST", "/api/auth/login", data=signin_data)
        if data and "token" in data:
            TOKEN = data["token"]
            click.echo(f"{Colors.GREEN}✅ {data['message']}{Colors.END}")
        else:
            click.echo(f"{Colors.RED}❌ Sign in failed: No token received.{Colors.END}")
    except click.ClickException as e:
        raise click.ClickException(f"{Colors.RED}❌ Sign in failed: {str(e)}{Colors.END}")

# Product Menu
# ... your existing imports and constants ...

def show_product_menu():
    """Display an interactive menu for product management."""
    while True:
        click.echo(f"\n{Colors.CYAN}{Colors.BOLD}╔══════════════════════════════════════════════╗{Colors.END}")
        click.echo(f"{Colors.CYAN}{Colors.BOLD}║              PRODUCT MANAGEMENT              ║{Colors.END}")
        click.echo(f"{Colors.CYAN}{Colors.BOLD}╚══════════════════════════════════════════════╝{Colors.END}")
        click.echo(f"{Colors.YELLOW}1.{Colors.END} {Colors.GREEN}📋 Display all products{Colors.END}")
        click.echo(f"{Colors.YELLOW}2.{Colors.END} {Colors.GREEN}🔍 Fetch a product by ID{Colors.END}")
        click.echo(f"{Colors.YELLOW}3.{Colors.END} {Colors.GREEN}➕ Add a new product{Colors.END}")
        click.echo(f"{Colors.YELLOW}4.{Colors.END} {Colors.GREEN}✏️  Update a product{Colors.END}")
        click.echo(f"{Colors.YELLOW}5.{Colors.END} {Colors.RED}🗑️  Delete a product{Colors.END}")
        click.echo(f"{Colors.YELLOW}6.{Colors.END} {Colors.GREEN}💰 Find expensive products{Colors.END}")
        click.echo(f"{Colors.YELLOW}7.{Colors.END} {Colors.BLUE}← Return to main menu{Colors.END}")

        choice = click.prompt(f"\n{Colors.BLUE}Enter your choice (1-7){Colors.END}", type=int)

        try:
            if choice == 1:
                list_products()
            elif choice == 2:
                fetch_product()
            elif choice == 3:
                add_product()
            elif choice == 4:
                update_product()
            elif choice == 5:
                delete_product()
            elif choice == 6:
                find_expensive_products()
            elif choice == 7:
                click.echo(f"{Colors.YELLOW}Returning to main menu...{Colors.END}")
                break
            else:
                click.echo(f"{Colors.RED}❌ Invalid choice. Please select 1-7.{Colors.END}")
        except click.ClickException as e:
            click.echo(f"{Colors.RED}{str(e)}{Colors.END}")
        except Exception as e:
            click.echo(f"{Colors.RED}❌ Unexpected error: {str(e)}{Colors.END}")

def find_expensive_products():
    """Find products above a specified price threshold."""
    click.echo(f"\n{Colors.GREEN}{Colors.BOLD}💰 FIND EXPENSIVE PRODUCTS{Colors.END}")

    # Get price threshold from user
    price_threshold = click.prompt(
        f"{Colors.BLUE}Enter the minimum price threshold{Colors.END}",
        type=float
    )

    # Validate the price threshold
    if price_threshold < 0:
        raise click.ClickException(f"{Colors.RED}❌ Price threshold cannot be negative.{Colors.END}")

    click.echo(f"{Colors.YELLOW}⏳ Searching for products above ${price_threshold:.2f}...{Colors.END}")

    # Make the API request with the price threshold parameter
    params = {"priceThreshold": price_threshold}
    data = make_request("GET", "/api/products/expensive", params=params)

    if data:
        if not data:
            click.echo(f"{Colors.YELLOW}📭 No expensive products found above ${price_threshold:.2f}.{Colors.END}")
            return

        # Display the results in a table
        table = [[item["id"], item["name"], f"{Colors.YELLOW}${item['price']:.2f}{Colors.END}",
                  item.get("expirationDate", f"{Colors.RED}N/A{Colors.END}")] for item in data]

        click.echo(f"\n{Colors.GREEN}{Colors.BOLD}💰 EXPENSIVE PRODUCTS (Above ${price_threshold:.2f}):{Colors.END}")
        click.echo(f"{Colors.CYAN}Found {len(data)} product(s){Colors.END}")
        click.echo(tabulate(table, headers=[f"{Colors.CYAN}ID{Colors.END}", f"{Colors.CYAN}Name{Colors.END}",
                                            f"{Colors.CYAN}Price{Colors.END}", f"{Colors.CYAN}Expiration Date{Colors.END}"],
                            tablefmt="grid"))
    else:
        click.echo(f"{Colors.YELLOW}📭 No expensive products found above ${price_threshold:.2f}.{Colors.END}")

# ... rest of your existing code (user menu functions, auth functions, etc.) ...

def list_products():
    """Display all products in a table."""
    click.echo(f"\n{Colors.GREEN}📦 Loading products...{Colors.END}")
    data = make_request("GET", "/api/products")
    if data:
        if not data:
            click.echo(f"{Colors.YELLOW}📭 No products found.{Colors.END}")
            return
        table = [[item["id"], item["name"], f"{Colors.YELLOW}${item['price']:.2f}{Colors.END}", 
                 item.get("expirationDate", f"{Colors.RED}N/A{Colors.END}")] for item in data]
        click.echo(f"\n{Colors.GREEN}{Colors.BOLD}📋 PRODUCT LIST:{Colors.END}")
        click.echo(tabulate(table, headers=[f"{Colors.CYAN}ID{Colors.END}", f"{Colors.CYAN}Name{Colors.END}", 
                                           f"{Colors.CYAN}Price{Colors.END}", f"{Colors.CYAN}Expiration Date{Colors.END}"], 
                           tablefmt="grid"))
    else:
        click.echo(f"{Colors.YELLOW}📭 No products returned.{Colors.END}")

def fetch_product():
    """Fetch a product by ID."""
    id = click.prompt(f"{Colors.BLUE}Enter product ID{Colors.END}", type=int)
    click.echo(f"{Colors.GREEN}🔍 Searching for product {id}...{Colors.END}")
    data = make_request("GET", f"/api/products/{id}")
    if data:
        click.echo(f"\n{Colors.GREEN}{Colors.BOLD}✅ PRODUCT FOUND:{Colors.END}")
        table = [[data["id"], data["name"], f"{Colors.YELLOW}${data['price']:.2f}{Colors.END}", 
                 data.get("expirationDate", f"{Colors.RED}N/A{Colors.END}")]]
        click.echo(tabulate(table, headers=[f"{Colors.CYAN}ID{Colors.END}", f"{Colors.CYAN}Name{Colors.END}", 
                                           f"{Colors.CYAN}Price{Colors.END}", f"{Colors.CYAN}Expiration Date{Colors.END}"], 
                           tablefmt="grid"))

def add_product():
    """Add a new product."""
    click.echo(f"\n{Colors.GREEN}{Colors.BOLD}➕ ADD NEW PRODUCT{Colors.END}")
    name = click.prompt(f"{Colors.BLUE}Enter product name{Colors.END}")
    price = click.prompt(f"{Colors.BLUE}Enter product price{Colors.END}", type=float)
    expiration_date = click.prompt(f"{Colors.BLUE}Enter expiration date (YYYY-MM-DD){Colors.END}", type=str)
    
    try:
        datetime.strptime(expiration_date, "%Y-%m-%d")
    except ValueError:
        raise click.ClickException(f"{Colors.RED}❌ Invalid date format. Use YYYY-MM-DD.{Colors.END}")
    
    # Create product WITHOUT ID - let the server generate it
    product = {"name": name, "price": price, "expirationDate": expiration_date}
    
    click.echo(f"{Colors.YELLOW}⏳ Creating product...{Colors.END}")
    data = make_request("POST", "/api/products", data=product)
    if data:
        click.echo(f"\n{Colors.GREEN}{Colors.BOLD}✅ PRODUCT CREATED:{Colors.END}")
        table = [[data["id"], data["name"], f"{Colors.YELLOW}${data['price']:.2f}{Colors.END}", 
                 data.get("expirationDate", f"{Colors.RED}N/A{Colors.END}")]]
        click.echo(tabulate(table, headers=[f"{Colors.CYAN}ID{Colors.END}", f"{Colors.CYAN}Name{Colors.END}", 
                                           f"{Colors.CYAN}Price{Colors.END}", f"{Colors.CYAN}Expiration Date{Colors.END}"], 
                           tablefmt="grid"))

def update_product():
    """Update a product by ID."""
    click.echo(f"\n{Colors.GREEN}{Colors.BOLD}✏️  UPDATE PRODUCT{Colors.END}")
    id = click.prompt(f"{Colors.BLUE}Enter product ID{Colors.END}", type=int)
    name = click.prompt(f"{Colors.BLUE}Enter new product name{Colors.END}")
    price = click.prompt(f"{Colors.BLUE}Enter new product price{Colors.END}", type=float)
    expiration_date = click.prompt(f"{Colors.BLUE}Enter new expiration date (YYYY-MM-DD){Colors.END}", type=str)
    
    try:
        datetime.strptime(expiration_date, "%Y-%m-%d")
    except ValueError:
        raise click.ClickException(f"{Colors.RED}❌ Invalid date format. Use YYYY-MM-DD.{Colors.END}")
    
    product = {"name": name, "price": price, "expirationDate": expiration_date}
    
    click.echo(f"{Colors.YELLOW}⏳ Updating product...{Colors.END}")
    data = make_request("PUT", f"/api/products/{id}", data=product)
    if data:
        click.echo(f"\n{Colors.GREEN}{Colors.Bold}✅ PRODUCT UPDATED:{Colors.END}")
        table = [[data["id"], data["name"], f"{Colors.YELLOW}${data['price']:.2f}{Colors.END}", 
                 data.get("expirationDate", f"{Colors.RED}N/A{Colors.END}")]]
        click.echo(tabulate(table, headers=[f"{Colors.CYAN}ID{Colors.END}", f"{Colors.CYAN}Name{Colors.END}", 
                                           f"{Colors.CYAN}Price{Colors.END}", f"{Colors.CYAN}Expiration Date{Colors.END}"], 
                           tablefmt="grid"))

def delete_product():
    """Delete a product by ID."""
    click.echo(f"\n{Colors.RED}{Colors.BOLD}🗑️  DELETE PRODUCT{Colors.END}")
    id = click.prompt(f"{Colors.BLUE}Enter product ID to delete{Colors.END}", type=int)
    
    if click.confirm(f"{Colors.RED}⚠️  Are you sure you want to delete product {id}? This action cannot be undone.{Colors.END}"):
        click.echo(f"{Colors.YELLOW}⏳ Deleting product...{Colors.END}")
        make_request("DELETE", f"/api/products/{id}")
        click.echo(f"{Colors.GREEN}✅ Product {id} deleted successfully.{Colors.END}")
    else:
        click.echo(f"{Colors.YELLOW}❌ Deletion cancelled.{Colors.END}")

# User Menu
def show_user_menu():
    """Display an interactive menu for user management."""
    while True:
        click.echo(f"\n{Colors.CYAN}{Colors.BOLD}╔══════════════════════════════════════════════╗{Colors.END}")
        click.echo(f"{Colors.CYAN}{Colors.BOLD}║               USER MANAGEMENT                ║{Colors.END}")
        click.echo(f"{Colors.CYAN}{Colors.BOLD}╚══════════════════════════════════════════════╝{Colors.END}")
        click.echo(f"{Colors.YELLOW}1.{Colors.END} {Colors.GREEN}📋 Display all users{Colors.END}")
        click.echo(f"{Colors.YELLOW}2.{Colors.END} {Colors.GREEN}🔍 Fetch a user by ID{Colors.END}")
        click.echo(f"{Colors.YELLOW}3.{Colors.END} {Colors.GREEN}➕ Add a new user{Colors.END}")
        click.echo(f"{Colors.YELLOW}4.{Colors.END} {Colors.GREEN}✏️  Update a user{Colors.END}")
        click.echo(f"{Colors.YELLOW}5.{Colors.END} {Colors.RED}🗑️  Delete a user{Colors.END}")
        click.echo(f"{Colors.YELLOW}6.{Colors.END} {Colors.BLUE}← Return to main menu{Colors.END}")
        
        choice = click.prompt(f"\n{Colors.BLUE}Enter your choice (1-6){Colors.END}", type=int)

        try:
            if choice == 1:
                list_users()
            elif choice == 2:
                fetch_user()
            elif choice == 3:
                add_user()
            elif choice == 4:
                update_user()
            elif choice == 5:
                delete_user()
            elif choice == 6:
                click.echo(f"{Colors.YELLOW}Returning to main menu...{Colors.END}")
                break
            else:
                click.echo(f"{Colors.RED}❌ Invalid choice. Please select 1-6.{Colors.END}")
        except click.ClickException as e:
            click.echo(f"{Colors.RED}{str(e)}{Colors.END}")
        except Exception as e:
            click.echo(f"{Colors.RED}❌ Unexpected error: {str(e)}{Colors.END}")

def list_users():
    """Display all users in a table."""
    click.echo(f"\n{Colors.GREEN}👥 Loading users...{Colors.END}")
    data = make_request("GET", "/api/users")
    if data:
        if not data:
            click.echo(f"{Colors.YELLOW}📭 No users found.{Colors.END}")
            return
        table = [[item["id"], item["name"], item["email"], 
                 f"{Colors.CYAN}{item.get('age', 'N/A')}{Colors.END}"] for item in data]
        click.echo(f"\n{Colors.GREEN}{Colors.Bold}📋 USER LIST:{Colors.END}")
        click.echo(tabulate(table, headers=[f"{Colors.CYAN}ID{Colors.END}", f"{Colors.CYAN}Name{Colors.END}", 
                                           f"{Colors.CYAN}Email{Colors.END}", f"{Colors.CYAN}Age{Colors.END}"], 
                           tablefmt="grid"))
    else:
        click.echo(f"{Colors.YELLOW}📭 No users returned.{Colors.END}")

def fetch_user():
    """Fetch a user by ID."""
    id = click.prompt(f"{Colors.BLUE}Enter user ID{Colors.END}", type=int)
    click.echo(f"{Colors.GREEN}🔍 Searching for user {id}...{Colors.END}")
    data = make_request("GET", f"/api/users/{id}")
    if data:
        click.echo(f"\n{Colors.GREEN}{Colors.Bold}✅ USER FOUND:{Colors.END}")
        table = [[data["id"], data["name"], data["email"], 
                 f"{Colors.CYAN}{data.get('age', 'N/A')}{Colors.END}"]]
        click.echo(tabulate(table, headers=[f"{Colors.CYAN}ID{Colors.END}", f"{Colors.CYAN}Name{Colors.END}", 
                                           f"{Colors.CYAN}Email{Colors.END}", f"{Colors.CYAN}Age{Colors.END}"], 
                           tablefmt="grid"))

def add_user():
    """Add a new user."""
    click.echo(f"\n{Colors.GREEN}{Colors.Bold}➕ ADD NEW USER{Colors.END}")
    name = click.prompt(f"{Colors.BLUE}Enter name{Colors.END}")
    email = click.prompt(f"{Colors.BLUE}Enter email{Colors.END}")
    password = click.prompt(f"{Colors.BLUE}Enter password{Colors.END}", hide_input=True)
    age = click.prompt(f"{Colors.BLUE}Enter age{Colors.END}", type=int)
    
    user = {"name": name, "email": email, "password": password, "age": age}
    
    click.echo(f"{Colors.YELLOW}⏳ Creating user...{Colors.END}")
    data = make_request("POST", "/api/users", data=user)
    if data:
        click.echo(f"\n{Colors.GREEN}{Colors.Bold}✅ USER CREATED:{Colors.END}")
        table = [[data["id"], data["name"], data["email"], 
                 f"{Colors.CYAN}{data.get('age', 'N/A')}{Colors.END}"]]
        click.echo(tabulate(table, headers=[f"{Colors.CYAN}ID{Colors.END}", f"{Colors.CYAN}Name{Colors.END}", 
                                           f"{Colors.CYAN}Email{Colors.END}", f"{Colors.CYAN}Age{Colors.END}"], 
                           tablefmt="grid"))

def update_user():
    """Update a user by ID."""
    click.echo(f"\n{Colors.GREEN}{Colors.Bold}✏️  UPDATE USER{Colors.END}")
    id = click.prompt(f"{Colors.BLUE}Enter user ID{Colors.END}", type=int)
    name = click.prompt(f"{Colors.BLUE}Enter new name{Colors.END}")
    email = click.prompt(f"{Colors.BLUE}Enter new email{Colors.END}")
    password = click.prompt(f"{Colors.BLUE}Enter new password (leave blank to keep unchanged){Colors.END}", 
                           default="", show_default=False)
    age = click.prompt(f"{Colors.BLUE}Enter new age{Colors.END}", type=int)
    
    user = {"name": name, "email": email, "password": password or None, "age": age}
    
    click.echo(f"{Colors.YELLOW}⏳ Updating user...{Colors.END}")
    data = make_request("PUT", f"/api/users/{id}", data=user)
    if data:
        click.echo(f"\n{Colors.GREEN}{Colors.Bold}✅ USER UPDATED:{Colors.END}")
        table = [[data["id"], data["name"], data["email"], 
                 f"{Colors.CYAN}{data.get('age', 'N/A')}{Colors.END}"]]
        click.echo(tabulate(table, headers=[f"{Colors.CYAN}ID{Colors.END}", f"{Colors.CYAN}Name{Colors.END}", 
                                           f"{Colors.CYAN}Email{Colors.END}", f"{Colors.CYAN}Age{Colors.END}"], 
                           tablefmt="grid"))
def find_expensive_products():
    """Find products above a specified price threshold."""
    click.echo(f"\n{Colors.GREEN}{Colors.BOLD}💰 FIND EXPENSIVE PRODUCTS{Colors.END}")

    # Get price threshold from user
    price_threshold = click.prompt(
        f"{Colors.BLUE}Enter the minimum price threshold{Colors.END}",
        type=float
    )

    # Validate the price threshold
    if price_threshold < 0:
        raise click.ClickException(f"{Colors.RED}❌ Price threshold cannot be negative.{Colors.END}")

    click.echo(f"{Colors.YELLOW}⏳ Searching for products above ${price_threshold:.2f}...{Colors.END}")

    # Make the API request with the price threshold parameter
    params = {"priceThreshold": price_threshold}
    data = make_request("GET", "/api/products/expensive", params=params)

    if data:
        if not data:
            click.echo(f"{Colors.YELLOW}📭 No expensive products found above ${price_threshold:.2f}.{Colors.END}")
            return

        # Display the results in a table
        table = [[item["id"], item["name"], f"{Colors.YELLOW}${item['price']:.2f}{Colors.END}",
                  item.get("expirationDate", f"{Colors.RED}N/A{Colors.END}")] for item in data]

        click.echo(f"\n{Colors.GREEN}{Colors.BOLD}💰 EXPENSIVE PRODUCTS (Above ${price_threshold:.2f}):{Colors.END}")
        click.echo(f"{Colors.CYAN}Found {len(data)} product(s){Colors.END}")
        click.echo(tabulate(table, headers=[f"{Colors.CYAN}ID{Colors.END}", f"{Colors.CYAN}Name{Colors.END}",
                                            f"{Colors.CYAN}Price{Colors.END}", f"{Colors.CYAN}Expiration Date{Colors.END}"],
                            tablefmt="grid"))
    else:
        click.echo(f"{Colors.YELLOW}📭 No expensive products found above ${price_threshold:.2f}.{Colors.END}")

def delete_user():
    """Delete a user by ID."""
    click.echo(f"\n{Colors.RED}{Colors.Bold}🗑️  DELETE USER{Colors.END}")
    id = click.prompt(f"{Colors.BLUE}Enter user ID to delete{Colors.END}", type=int)
    
    if click.confirm(f"{Colors.RED}⚠️  Are you sure you want to delete user {id}? This action cannot be undone.{Colors.END}"):
        click.echo(f"{Colors.YELLOW}⏳ Deleting user...{Colors.END}")
        make_request("DELETE", f"/api/users/{id}")
        click.echo(f"{Colors.GREEN}✅ User {id} deleted successfully.{Colors.END}")
    else:
        click.echo(f"{Colors.YELLOW}❌ Deletion cancelled.{Colors.END}")

# Main CLI group
@click.group()
def cli():
    """CLI for managing Products and Users via REST API."""
    pass

cli.add_command(show_welcome_menu, name="start")

if __name__ == "__main__":
    show_welcome_menu()