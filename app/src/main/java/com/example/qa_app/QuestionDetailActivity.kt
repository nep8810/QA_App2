package com.example.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*

class QuestionDetailActivity : AppCompatActivity(),View.OnClickListener{

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAnswerRef: DatabaseReference

    // データに追加・変化があった時に受け取るChildEventListenerを作成
    // onChildAddedメソッドは、要素が追加された時(今回は回答が追加された時)に呼ばれる
    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        // 要素に変化があった時(今回は質問に対して回答が投稿された時)に呼ばれるメソッド
        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        // 要素が削除された時に呼ばれるメソッド
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        // 場所の優先度が変更された時に呼ばれるメソッド
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        // エラーが起きて(サーバーで失敗orセキュリティルール/Firebaseルールの結果として)削除された時に呼ばれる
        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面に遷移させる
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

        // - - - ↓ ログイン時にお気に入りボタンを表示 ↓- - -
        setContentView(R.layout.activity_question_detail)

        // findViewById()→setContentView()の順にしないとViewが用意されていないのにfindViewすることになるのでnullが返る
        val FB = this.findViewById<Button>(R.id.favorite_button);FB.setOnClickListener(this)
        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // ログインしていなければ★は表示しない(A.setVisibility(View.INVISIBLE)でAを非表示)
            FB.setVisibility(View.INVISIBLE)
        } else {
            // ログインしていれば★を表示する
        }
        return
        // - - - ↑ ログイン時にお気に入りボタンを表示 ↑- - -
    }

    // - - - ↓ お気に入りがタップされた時の処理↓ - - -
    override fun onClick(v: View){
        val FB = this.findViewById<Button>(R.id.favorite_button)
        var isFavorite = false
        if (isFavorite == true) {
            FB.setText(R.string.label1)

        } else if(isFavorite == false){
            FB.setText(R.string.label2)

        }else{
            // ログイン後、初タップ
            FB.setText(R.string.label1)
        }
    }    // - - - ↑ お気に入りがタップされた時の処理 ↑ - - -

    // - - - ↓ 他のアクティビティから戻ってきたときの処理 ↓ - - -
    override fun onResume() {
        super.onResume()
        val FB = this.findViewById<Button>(R.id.favorite_button)
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // ログインしていなければ★は表示しない
        } else {
            // ログインしていれば★を表示する
            FB.setVisibility(View.VISIBLE)
        }
    }
    // - - - ↑ 他のアクティビティから戻ってきたときの処理 ↑ - - -
}
