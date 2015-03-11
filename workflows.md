# Yobi 프로젝트 워크플로우

## 브랜치

* master - 안정화된 브랜치. 다음번 메인터넌스 릴리즈에 포함될 코드를 작성한다.
* next - 일반적으로 개발을 진행하는 브랜치. 다음번 기능 릴리즈에 포함될 코드를
  작성한다.
* pu - 테스트용 브랜치
* internal - 팀 내부용 파일(정책 문서 등)들을 보관하기 위한 브랜치

## 코드 보내기와 머지하기

* Yobi 코드를 고치려면, topic 브랜치를 Yobi 프로젝트의 저장소에 만들어 작업하고
  Yobi의 코드 보내기 기능으로 보낸다.
* 핫픽스는 master로 코드를 보낸다. 단, 메인테이너가 요청한다면(주로 충돌이
  발생한 경우) next에도 코드를 보낼 수 있다.
* 핫픽스 이외의 코드는 next로 보낸다.
* 메인테이너는 필요에 따라 master에 머지된 핫픽스를 next에도 머지한다. 충돌이
  발생한 경우, 해당 핫픽스를 한 사람에게 충돌을 해결하여 next로 코드를
  보내달라고 요청한다.

## 릴리즈

* 릴리즈에는 메인터넌스 릴리즈와 기능 릴리즈가 있다.
* 모든 릴리즈는 master에서 태깅하여 릴리즈한다.

### 메인터넌스 릴리즈

* 이전 릴리즈에서 버그만을 고친 안정적인 릴리즈
* 버전은 X.Y.Z(Z >= 1) 형식으로 붙인다. (예: 0.5.1, 0.5.2, 1.0.1)

### 기능 릴리즈

* 이전 릴리즈에서 기능이 추가/개선된 릴리즈
* 버전은 X.Y.0 형식으로 붙인다. (예: 0.6.0, 0.7.0, 1.0.0)
* 기능 릴리즈 시기가 임박하면 next 브랜치도 master 브랜치처럼 안정화 작업을
  시작한다. 즉, 기능추가 없이 버그수정 작업만을 머지한다.
* next가 기능 릴리즈를 릴리즈 할 수 있을 만큼 안정적이 되었다면 메인테이너가
  master에 머지하고 릴리즈한다.

### 릴리즈 프로세스

1. X.Y.Z 형식으로 버전을 확정한다.
2. docs/relnotes/{버전}.txt 파일로 릴리즈 노트를 작성한다. (예:
   docs/relnotes/0.6.0.txt)
3. conf/version.conf 파일에 기록되어있는 버전을 릴리즈할 버전으로 고친다.
4. 릴리즈할 커밋을 v{버전} 형식으로 태깅한다. (예: v0.6.0)
5. http://yobi.io/yobi.zip 로 Yobi를 다운로드 받을 수 있도록 설치 패키지를
   업로드한다.
6. github에 push하여 릴리즈한다.

### 주의사항

* Yobi 버전은 **반드시** [semver][] 형식을 따라야한다. Yobi 업데이트 알림
  기능이 업데이트가 필요한지 확인하기 위해 현재 버전과 최신 버전을 비교할
  때 각 버전이 semver 형식을 따르고 있는 것을 가정하고 있기 때문이다.

[semver]: http://semver.org/

## 참고 자료

* How to maintain Git
  https://www.kernel.org/pub/software/scm/git/docs/howto/maintain-git.txt
* gitworkflows(7) Manual Page
  https://www.kernel.org/pub/software/scm/git/docs/gitworkflows.html
* Submitting Patches
  https://www.kernel.org/pub/software/scm/git/docs/SubmittingPatches
* Semantic Versioning 2.0.0
  http://semver.org/
