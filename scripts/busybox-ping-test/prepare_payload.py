#!/usr/bin/python3
import requests
import time
import json
from helper_functions import *

ip_mac_db = {}

def put_httprequest(url, data=""):
  try:
     headers = {
               'Content-Type': 'application/json',
               'Accept': '*/*',
              }
     print(url,data)
     response = requests.put(url, data = json.dumps(data), headers=headers)
     if(response.ok):
       print("PUT Success", url)
     else:
       response.raise_for_status()
  except requests.exceptions.HTTPError as err:
     print("POST Failed for {} with error".format(url, response.text))
  

def post_httprequest(url, data=""):
  try:
     headers = {
               'Content-Type': 'application/json',
               'Accept': '*/*',
              }
     print(url,data)
     response = requests.post(url, data = json.dumps(data), headers=headers)
     if(response.ok):
       print("POST Success", url)
       if 'ports' in url:
         valid_response = json.loads(response.text,object_pairs_hook=dict_clean)
         get_mac_for_ips(valid_response)
     else:
       response.raise_for_status()
  except requests.exceptions.HTTPError as err:
     print("POST Failed for {} with error".format(url, response.text))
     print(response.json)
     print("ERROR",err)
     raise SystemExit(err)

def get_mac_for_ips(valid_response):
  print("in prepare_payload ",valid_response)
  ports_info = valid_response["port"]
  key = ports_info["fixed_ips"][0]["ip_address"]
  value =  ports_info["mac_address"]
  ip_mac_db[key] = value
  print(ip_mac_db)


def get_httprequest(url):
  try:
     response = requests.get(url)
     if(response.ok):
       print("GET Success", url)
       return response.text
     else:
       response.raise_for_status()
  except requests.HTTPError as exception:
  #except:requests.exceptions.HTTPError as e:
       print("GET failed for url", url)
       raise SystemExit(exception)


def get_mac_for_ips(valid_response):
   ports_info = valid_response["port"]
   key = ports_info["fixed_ips"][0]["ip_address"]
   value =  ports_info["mac_address"]
   ip_mac_db[key] = value

def get_mac_from_db():
   print("\n\n\n>>>>>>>")
   print("IP & MAC stored in ignite db", ip_mac_db)
   return ip_mac_db

