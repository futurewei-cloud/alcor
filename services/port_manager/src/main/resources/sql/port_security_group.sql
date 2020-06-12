CREATE TABLE IF NOT EXISTS t_port_security_group (
   id VARCHAR, security_group_id VARCHAR, port_id VARCHAR, PRIMARY KEY(id, port_id))
  WITH "affinityKey=port_id"