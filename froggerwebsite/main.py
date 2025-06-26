from fastapi import Request, Response
from fastapi.templating import Jinja2Templates
from fastapi.staticfiles import StaticFiles
from concurrent.futures import ThreadPoolExecutor
import asyncio
import jinja2
import os

from routes import create_app
from func.user import userTable

app = create_app()
app.mount("/static", StaticFiles(directory="static"), name="static")
templates = Jinja2Templates(directory="templates")

@app.middleware("http")
async def no_cache_middleware(request: Request, call_next):
  response: Response = await call_next(request)
  response.headers["Cache-Control"] = "no-store, no-cache, must-revalidate, proxy-revalidate"
  response.headers["Pragma"] = "no-cache"
  response.headers["Expires"] = "0"
  return response

# sessionkey = os.environ['sessionkey']

if __name__ == "__main__":
  asyncio.run(userTable())
  os.system("uvicorn main:app --host 0.0.0.0 --port 81 --reload")
