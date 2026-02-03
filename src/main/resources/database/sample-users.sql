-- Users
-- password: admin123
INSERT INTO users (username, password_hash, full_name, email, role, status, created_date)
VALUES ('admin', '$2a$10$H4N4yotNv.6gBKSKMGa/WOajMmBnqhGCJWmgzelOdm9DVq80sglZu', 'Administrator', 'admin@bank.com', 'ADMIN', 'ACTIVE', NOW());

-- password: manager123
INSERT INTO users (username, password_hash, full_name, email, role, status, created_date)
VALUES ('manager01', '$2a$10$iMr8zPUaxnJ8pAjLkls66uefyHzUn8WbjqqI.wHFzlrd0rfaWp6IW', 'Tran Van Manager', 'manager@bank.com', 'MANAGER', 'ACTIVE', NOW());

-- password: staff123
INSERT INTO users (username, password_hash, full_name, email, role, status, created_date)
VALUES ('staff01', '$2a$10$6RQgfJ2GW.2bw9TIZnn0u.1dxizousFNzUUpuCEqqcSSUFOej0rfaWp6IW', 'Nguyen Van Staff', 'staff@bank.com', 'STAFF', 'ACTIVE', NOW());
