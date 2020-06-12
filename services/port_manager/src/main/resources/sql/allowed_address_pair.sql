CREATE TABLE IF NOT EXISTS t_allowed_address_pair (
  id VARCHAR, ip_address VARCHAR, mac_address VARCHAR, port_id VARCHAR, PRIMARY KEY(id, port_id))
  WITH "affinityKey=port_id"