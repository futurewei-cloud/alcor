CREATE TABLE IF NOT EXISTS t_fixed_ip (
  id VARCHAR, subnet_id VARCHAR, ip_address VARCHAR, port_id VARCHAR, PRIMARY KEY(id, port_id))
  WITH "template=replicated, affinity_key=port_id"