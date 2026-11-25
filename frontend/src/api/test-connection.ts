import api from './client';

export async function testBackendConnection(): Promise<{ ok: boolean; message: string }> {
  try {
    const response = await api.get('/users/username-availability', {
      params: { username: 'fitstud_test' },
      timeout: 8000, // fail fast instead of hanging forever
    });

    console.log('Backend connected:', response.data);
    return { ok: true, message: `Connected: ${JSON.stringify(response.data)}` };
  } catch (error: any) {
    const status = error.response?.status;
    const detail = error.response?.data ?? error.message;
    console.log('Backend connection failed:', status, detail);
    console.log('baseURL:', api.defaults.baseURL);
  console.log('code:', error.code, 'message:', error.message);
  console.log('status:', error.response?.status);
    return {
      ok: false,
      message: `Failed${status ? ` (${status})` : ''}: ${
        typeof detail === 'string' ? detail : JSON.stringify(detail)
      }`,
    };
  }
}