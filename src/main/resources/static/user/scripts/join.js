const joinForm = document.getElementById('joinForm');

joinForm.onsubmit = function (e) {
	e.preventDefault();

	const xhr = new XMLHttpRequest();
	const formData = new FormData();
	formData.append('email', joinForm['email'].value);
	formData.append('name', joinForm['name'].value);
	formData.append('nickname', joinForm['nickname'].value);
	formData.append('password', joinForm['password'].value);
	formData.append('birthStr', joinForm['birthStr'].value);
	xhr.onreadystatechange = () => {
		if (xhr.readyState !== XMLHttpRequest.DONE) {
			return;
		}
		if (xhr.status < 200 || xhr.status >= 300) {
			return;
		}
		const responseObject = xhr.response;
        console.log(typeof responseObject);
	};
	xhr.open('POST', './join');
	xhr.send(formData);
};
