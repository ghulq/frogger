from fastapi import APIRouter, Request
from fastapi.responses import HTMLResponse
from fastapi.responses import RedirectResponse
from fastapi.responses import FileResponse
from fastapi.templating import Jinja2Templates
from fastapi.staticfiles import StaticFiles
from concurrent.futures import ThreadPoolExecutor
from urllib.parse import unquote
from datetime import datetime, timedelta
import asyncio
import jinja2
import json

main = APIRouter()
templates = Jinja2Templates(directory="templates")
executor = ThreadPoolExecutor()

from func.user import getLeaderboard, getUser

async def render_template(template_name: str, context: dict):
  return await asyncio.get_event_loop().run_in_executor(
    executor, lambda: templates.get_template(template_name).render(context)
  )

@main.get("/")
async def index(request: Request):
  user = request.cookies.get("username")
  if user is None:
    user = None
  leaderboard = await getLeaderboard()
  content = await render_template("index.html", {"request": request, "username": user, "leaderboard": leaderboard})
  return HTMLResponse(content)

@main.get("/login")
async def login(request: Request):
  content = await render_template("login.html", {"request": request})
  return HTMLResponse(content)

@main.get("/register")
async def register(request: Request):
  content = await render_template("login.html", {"request": request})
  return HTMLResponse(content)

@main.get("/logout")
async def logout(request: Request):
  response = RedirectResponse(url="/", status_code=303)
  response.delete_cookie(key="username", path="/")
  return response

@main.get("/@{username}")
async def user(request: Request, username: str):
  user = request.cookies.get("username")
  if user is None:
    user = None
  userc = await getUser(username)
  content = await render_template("profile.html", {"request": request, "username": user, "user": username, "userc": userc})
  return HTMLResponse(content)

@main.get("/download")
async def download(request: Request):
  return FileResponse("Frogger.jar", media_type="application/jar", filename="Frogger.jar")