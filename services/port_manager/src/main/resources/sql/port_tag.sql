CREATE TABLE IF NOT EXISTS t_port_tag (
  id VARCHAR, tag VARCHAR, port_id VARCHAR, PRIMARY KEY(id, port_id))
  WITH "affinityKey=port_id"