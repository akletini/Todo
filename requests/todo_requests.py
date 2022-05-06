import requests, json


request_path = "http://localhost:8080/todo/api/v1"


def get_all_todos():
    response = requests.get(f"{request_path}/todos")

    with open("todos.json", "w", encoding="utf-8") as f:
        json.dump(response.json(), f, ensure_ascii=False, indent=4)


def get_all_tags():
    response = requests.get(f"{request_path}/tags")

    with open("tags.json", "w", encoding="utf-8") as f:
        json.dump(response.json(), f, ensure_ascii=False, indent=4)


def add_todos():
    with open("todos.json", "r", encoding="utf-8") as f:
        data = json.load(f)
    requests.post(f"{request_path}/todos", json=data)


def add_tags():
    with open("tags.json", "r", encoding="utf-8") as f:
        data = json.load(f)
    requests.post(f"{request_path}/tags", json=data)
