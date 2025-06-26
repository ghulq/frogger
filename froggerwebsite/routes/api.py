from fastapi import APIRouter, Request, Header, HTTPException, Query, Form
from fastapi.responses import JSONResponse, RedirectResponse
import aiohttp
from aiohttp import ClientTimeout
from datetime import datetime, timedelta
import asyncio
import json
import re

from func.user import addUser, loginUser, updateScore

api = APIRouter()

@api.post("/api/v0/user/login")
async def login_user(request: Request):
  if request.cookies.get("username") is not None:
    raise HTTPException(status_code=400, detail="Already logged in")
  data = await request.json()
  if not data:
    raise HTTPException(status_code=400, detail="No data provided")
  username = data.get("username")
  password = data.get("password")
  if not username or not password:
    raise HTTPException(status_code=400, detail="Username or password not provided")
  try:
    id = await loginUser(username, password)
    if not id:
      raise Exception("Failed to login user")
  except Exception as e:
    print(e)
    raise HTTPException(status_code=500, detail=str(e))
  response = RedirectResponse(url="/", status_code=303)
  response.set_cookie(key="username", value=str(username), httponly=True, path="/")
  return response

@api.post("/api/v0/user/register")
async def add_user(request: Request):
  if request.cookies.get("username") is not None:
    raise HTTPException(status_code=400, detail="Already logged in")
  data = await request.json()
  if not data:
    raise HTTPException(status_code=400, detail="No data provided")
  username = data.get("username")
  password = data.get("password")
  if not username or not password:
    raise HTTPException(status_code=400, detail="Username or password not provided")
  try:
    id = await addUser(username, password)
    if not id:
      raise Exception("Failed to register user")
  except Exception as e:
    print(e)
    raise HTTPException(status_code=500, detail=str(e))
  response = RedirectResponse(url="/", status_code=303)
  response.set_cookie(key="username", value=str(username), httponly=True, path="/")
  return response

@api.post("/api/v0/user/addgame")
async def add_game(request: Request):
  data = await request.json()
  if not data:
    raise HTTPException(status_code=400, detail="No data provided")
  username = data.get("username")
  if not username:
    raise HTTPException(status_code=400, detail="username not provided")
  score = data.get("score")
  try:
    await updateScore(username, score)
    return JSONResponse(status_code=200, content={"message": "Game added"})
  except Exception as e:
    print(e)
    raise HTTPException(status_code=500, detail=str(e))