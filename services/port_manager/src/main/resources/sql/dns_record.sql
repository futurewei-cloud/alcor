CREATE TABLE IF NOT EXISTS t_dns_record (
  id VARCHAR, host_name VARCHAR, ip_address VARCHAR, fqdn VARCHAR, port_id VARCHAR, PRIMARY KEY(id, port_id))
  WITH "affinityKey=port_id"