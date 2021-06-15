#!/usr/bin/python3

# MIT License
# Copyright(c) 2020 Futurewei Cloud
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files(the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

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
     print("PUTing http request")
     print(url, data)
     response = requests.put(url, data=json.dumps(data), headers=headers)
     if(response.ok):
       print("PUT Success", url)
     else:
       response.raise_for_status()
  except requests.exceptions.HTTPError as err:
     print("PUT Failed for {} with error".format(url, response.text))


def post_httprequest(url, data=""):
  try:
     headers = {
               'Content-Type': 'application/json',
               'Accept': '*/*',
               }
     print("POSTing http request")
     print(url, data)
     response = requests.post(url, data=json.dumps(data), headers=headers)
     if(response.ok):
       print("POST Success", url)
       if 'ports' in url:
         valid_response = json.loads(response.text, object_pairs_hook = dict_clean)
         get_mac_for_ips(valid_response)
         print("POST RESPONSE: {}".format(valid_response))
     else:
       response.raise_for_status()
  except requests.exceptions.HTTPError as err:
     print("POST Failed for {} with error".format(url, response.text))
     print(response.json)
     print("ERROR",err)
     raise SystemExit(err)


def get_mac_for_ips(valid_response):
  print("in prepare_payload ", valid_response)
  ports_info = valid_response["port"]
  key = ports_info["fixed_ips"][0]["ip_address"]
  value =  ports_info["mac_address"]
  ip_mac_db[key] = value
  print("IP_MAC_DB = ", ip_mac_db)


def get_httprequest(url):
  try:
     response = requests.get(url)
     if(response.ok):
       print("GET Success", url)
       return response.text
     else:
       response.raise_for_status()
  except requests.HTTPError as exception:
     print("GET failed for url", url)
     raise SystemExit(exception)


def get_mac_from_db():
   print("\n\n\n>>>>>>>")
   print("IP & MAC stored in ignite db", ip_mac_db)
   return ip_mac_db

