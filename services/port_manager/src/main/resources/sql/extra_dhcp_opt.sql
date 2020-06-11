CREATE TABLE IF NOT EXISTS t_extra_dhcp_opt (
  id VARCHAR, ip_version VARCHAR, opt_name VARCHAR, opt_value VARCHAR, port_id VARCHAR, PRIMARY KEY(id, port_id))
  WITH "affinityKey=port_id"