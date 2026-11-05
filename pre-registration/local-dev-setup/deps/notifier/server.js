'use strict';

const http = require('http');
const net = require('net');
const { URL } = require('url');

const SERVER_PORT = Number(process.env.SERVER_PORT || 8083);
const SMTP_HOST = process.env.SMTP_HOST || 'mock-smtp';
const SMTP_PORT = Number(process.env.SMTP_PORT || 8025);
const SMS_URL = process.env.SMS_URL || 'http://mock-smtp:8080/sendsms';
const MAIL_FROM = process.env.MAIL_FROM || 'do-not-reply@mosip.io';

function readBody(req) {
	return new Promise((resolve, reject) => {
		const chunks = [];
		req.on('data', (c) => chunks.push(c));
		req.on('end', () => resolve(Buffer.concat(chunks).toString('utf8')));
		req.on('error', reject);
	});
}

function formField(body, name) {
	const re = new RegExp(
		`name="${name}"[^\\r\\n]*\\r?\\n(?:[^\\r\\n]+:[^\\r\\n]*\\r?\\n)*\\r?\\n([\\s\\S]*?)\\r?\\n--`,
		'i'
	);
	const m = body.match(re);
	return m ? m[1].trim() : '';
}

function jsonOk(res, status, message) {
	const payload = JSON.stringify({
		id: 'mosip.notification',
		version: '1.0',
		responsetime: new Date().toISOString(),
		response: { status: status || 'success', message: message || 'sent' },
		errors: null
	});
	res.writeHead(200, { 'Content-Type': 'application/json' });
	res.end(payload);
}

function jsonHealth(res) {
	res.writeHead(200, { 'Content-Type': 'application/json' });
	res.end(JSON.stringify({ status: 'UP' }));
}

function readLine(socket) {
	return new Promise((resolve, reject) => {
		let buf = '';
		const onData = (d) => {
			buf += d.toString('utf8');
			const idx = buf.indexOf('\n');
			if (idx >= 0) {
				socket.off('data', onData);
				socket.off('error', onErr);
				resolve(buf);
			}
		};
		const onErr = (err) => {
			socket.off('data', onData);
			socket.off('error', onErr);
			reject(err);
		};
		socket.on('data', onData);
		socket.on('error', onErr);
	});
}

async function smtpSend(to, subject, content) {
	const socket = net.connect(SMTP_PORT, SMTP_HOST);
	const expect = async (prefix) => {
		const line = await readLine(socket);
		if (!line.startsWith(prefix)) {
			throw new Error('SMTP: ' + line.trim());
		}
	};
	const cmd = (s) => socket.write(s);
	await expect('220');
	cmd('HELO prereg-local\r\n');
	await expect('250');
	cmd(`MAIL FROM:<${MAIL_FROM}>\r\n`);
	await expect('250');
	cmd(`RCPT TO:<${to}>\r\n`);
	await expect('250');
	cmd('DATA\r\n');
	await expect('354');
	const escaped = String(content || '').replace(/\r?\n\./g, '\n..');
	cmd(
		`From: ${MAIL_FROM}\r\nTo: ${to}\r\nSubject: ${subject || 'MOSIP'}\r\nMIME-Version: 1.0\r\nContent-Type: text/html; charset=UTF-8\r\n\r\n${escaped}\r\n.\r\n`
	);
	await expect('250');
	cmd('QUIT\r\n');
	socket.end();
}

function sendSms(number, message) {
	const url = new URL(SMS_URL);
	url.searchParams.set('mobiles', number || '');
	url.searchParams.set('sender', 'MOSIP');
	url.searchParams.set('message', message || '');
	return new Promise((resolve, reject) => {
		http.get(url, (r) => {
			r.resume();
			r.on('end', resolve);
			r.on('error', reject);
		}).on('error', reject);
	});
}

const server = http.createServer(async (req, res) => {
	try {
		const path = (req.url || '/').split('?')[0];
		if (req.method === 'GET' && (path === '/' || path.endsWith('/actuator/health'))) {
			jsonHealth(res);
			return;
		}
		if (req.method === 'POST' && path.endsWith('/email/send')) {
			const body = await readBody(req);
			const to = formField(body, 'mailTo') || formField(body, 'mailTo[0]');
			const subject = formField(body, 'mailSubject');
			const content = formField(body, 'mailContent');
			if (!to) {
				throw new Error('mailTo missing');
			}
			await smtpSend(to, subject, content);
			jsonOk(res, 'success', 'Email Request submitted');
			return;
		}
		if (req.method === 'POST' && path.endsWith('/sms/send')) {
			const body = JSON.parse((await readBody(req)) || '{}');
			const number = body.request && body.request.number;
			const message = body.request && body.request.message;
			await sendSms(number, message);
			jsonOk(res, 'success', 'Sms Request Sent');
			return;
		}
		res.writeHead(404, { 'Content-Type': 'application/json' });
		res.end(JSON.stringify({ errors: [{ errorCode: 'NOT_FOUND', message: path }] }));
	} catch (err) {
		console.error(err);
		res.writeHead(500, { 'Content-Type': 'application/json' });
		res.end(JSON.stringify({
			errors: [{ errorCode: 'NOTIFY_FAIL', message: String(err.message || err) }]
		}));
	}
});

server.listen(SERVER_PORT, '0.0.0.0', () => {
	console.log(`notifier on :${SERVER_PORT} -> smtp ${SMTP_HOST}:${SMTP_PORT}`);
});
