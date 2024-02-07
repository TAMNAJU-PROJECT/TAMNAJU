const joinForm = document.getElementById('joinForm');

joinForm.onsubmit = function (e) {
	e.preventDefault();

	const xhr = new XMLHttpRequest();
	const formData = new FormData();
	formData.append('id', joinForm['id'].value);
	formData.append('email', joinForm['email'].value);
	formData.append('name', joinForm['name'].value);
	formData.append('password', joinForm['password'].value);
	formData.append('birthStr', joinForm['birthStr'].value);
	xhr.onreadystatechange = () => {
		if (xhr.readyState !== XMLHttpRequest.DONE) {
			return;
		}
		if (xhr.status < 200 || xhr.status >= 300) {
			return;
		}
		const response = JSON.parse(xhr.response);

		let messages = document.querySelectorAll('.message');
		for (const message of messages) {
			message.innerText = '';
			message.classList.remove('visible');
		}
		let result = document.querySelector('[rel="m-result"]');
		switch (response['result']) {
			case 'failure':
				for (const [key, value] of Object.entries(response)) {
					if (key !== 'result') {
						let message = document.querySelector(
							`[rel = \"m-${key}\"]`
						);
						message.innerText = `${value}`;
						message.classList.add('visible');
					}
				}
				break;
			case 'failure_duplicate_id':
				result.innerText = '이미 가입된 ID 입니다.';
				break;
			case 'failure_duplicate_email':
				result.innerText = '이미 가입된 Email 입니다.';
				break;
			case 'success':
				window.location.href = '/login';
				break;
			default:
				break;
		}
	};
	xhr.open('POST', './join');
	xhr.send(formData);
};
