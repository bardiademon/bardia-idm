<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<link rel="shortcut icon" href="images/ic-website.png" type="image/png" />
	<title>bardiademon</title>
	<meta name="google-site-verification" content="W9T2lEMBlBiI_gfbJGpHQWOVqINKRrVBwsTXxCnRP9g" />
	
	<link rel="stylesheet" href="https://bardiademon.com/style.css" type="text/css" />
</head>
<body>
<header>
	<h1 class="text-bardiademon">bardiademon</h1>
	<div class="div-code">
		<div class="div-code-header">
			<img src="https://bardiademon.com/images/ic-java.png" class="div-code-header-ic" alt="java" />
			<div>Bardiademon.java</div>
		</div>
		<hr />
		<code>
			<br />
			<label class="keyword-java">package</label>
			com.bardiademon;
			<br /><br />
			<label class="keyword-java">public</label>
			<label class="keyword-java">final</label>
			<label class="keyword-java">class</label>
			Bardiademon<br />
			{<br />
			<span></span>
			<label class="keyword-java">private</label>
			<label class="keyword-java">final</label>
			Jop <label class="instance-final-field">JOP</label> =
			<label class="keyword-java">new</label>
			Developer (
			<label class="param-lbl">usingFrameworks:</label>
			<label class="keyword-java">true</label> ,
			Lang.<label class="static-final-field">JAVA</label> , Lang.<label class="static-final-field">JS</label>);<br /><br />

			<span></span>
			<label class="keyword-java">public</label>
			Jop job ()<br />
			<span></span>{<br />
			<span></span><span></span>
			<label class="keyword-java">return</label>
			<label class="instance-final-field">JOP</label>;<br />
			<span></span>}<br /><br />
			<span></span>
			<label class="keyword-java">public</label>
			String email () <br />
			<span></span>{<br />
			<span></span><span></span>
			<label class="keyword-java">return</label>
			<label class="string-text">"bardiademon@gmail.com"</label>;<br />
			<span></span>}<br /><br />
			<span></span>
			<label class="keyword-java">public</label>
			String phone () <br />
			<span></span>{<br />
			<span></span><span></span>
			<label class="keyword-java">return</label>
			<label class="string-text">"+989170221393"</label>;<br />
			<span></span>}<br />
			}<br />
		</code>
	</div>

</header>
<div class="i-m-on">
	<div class="i-m-on-rows">
		<div>
			<a class="i-m-on-img-a" href="https://github.com/bardiademon" target="_blank">
				<img class="i-m-on-img" src="https://bardiademon.com/images/ic-github.png" alt="GitHub" />
				Github
			</a>
			<a class="i-m-on-img-a" href="https://gitlab.com/bardiademon" target="_blank">
				<img class="i-m-on-img" src="https://bardiademon.com/images/ic-gitlab.png" alt="GitLab" />
				GitLab
			</a>
		</div>
		<div>
			<a class="i-m-on-img-a" href="https://t.me/bardiademon" target="_blank">
				<img class="i-m-on-img" src="https://bardiademon.com/images/ic-telegram.png" alt="Telegram" />
				Telegram
			</a>
			<a class="i-m-on-img-a" href="https://instagram.com/bardiademon.ir" target="_blank">
				<img class="i-m-on-img" src="https://bardiademon.com/images/ic-instagram.png" alt="Instagram" />
				Instagram
			</a>
		</div>
	</div>
</div>
</body>
</html>