CREATE TABLE IF NOT EXISTS t_port (
  id VARCHAR PRIMARY KEY, name VARCHAR, project_id VARCHAR, description VARCHAR, vpc_id VARCHAR, tenant_id VARCHAR,
  admin_state_up BOOLEAN, mac_address VARCHAR, veth_name VARCHAR, fast_path BOOLEAN, device_id VARCHAR, device_owner VARCHAR,
  status VARCHAR, binding_host_id VARCHAR, binding_profile VARCHAR, binding_vif_details VARCHAR, binding_vif_type VARCHAR,
  binding_vnic_type VARCHAR, network_ns VARCHAR, dns_name VARCHAR, dns_domain VARCHAR, create_at TIMESTAMP, update_at TIMESTAMP,
  ip_allocation VARCHAR, port_security_enabled BOOLEAN, qos_network_policy_id VARCHAR, qos_policy_id VARCHAR, revision_number INT,
  resource_request INT, uplink_status_propagation BOOLEAN, mac_learning_enabled BOOLEAN)
  WITH "template=replicated"
