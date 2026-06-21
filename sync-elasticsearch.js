const http = require('http');

// Assuming you have an admin user in your DB, replace with your admin credentials
const adminCredentials = {
  email: 'admin@library.com', // Replace if different
  password: 'admin'           // Replace if different
};

async function syncElasticsearch() {
  console.log('1. Fetching Admin Token...');
  
  try {
    const loginRes = await fetch('http://localhost:8081/api/user/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(adminCredentials)
    });
    
    if (!loginRes.ok) {
        throw new Error(`Login failed! Status: ${loginRes.status} Body: ${await loginRes.text()}`);
    }
    
    const loginData = await loginRes.json();
    const token = loginData.data.tokens.accessToken;
    console.log('Token acquired. Initiating Sync...');
    
    const syncRes = await fetch('http://localhost:8082/api/book/admin/sync-search', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await syncRes.text();
    console.log(`Sync Result [${syncRes.status}]:`, result);
    
  } catch (err) {
    console.error('Error during sync:', err.message);
    console.log('Please make sure your services are running via start-all.bat!');
  }
}

syncElasticsearch();
