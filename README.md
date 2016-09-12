# android letters

[![](https://jitpack.io/v/keygenqt/android-letters.svg)](https://jitpack.io/#keygenqt/android-letters)

My android letters

Add it to your build.gradle with:
```gradle
allprojects {
    repositories {
        // ...
        maven { url "https://jitpack.io" }
    }
}
```
and:

```gradle
dependencies {
    compile 'com.github.keygenqt:android-letters:1.0.1'
}
```

## Usage

Init:

```java
// View:
List<User> list = new ArrayList<>();
Letters letters = new Letters(activity, "name", new ArrayList<>(list));
letters.setOnSelect(new Letters.OnSelect() {
    @Override
    public void onSelect(int index, String letter) {
        ((RecyclerView) view.findViewById(R.id.rv)).getLayoutManager().scrollToPosition(index);
    }
});
((FrameLayout) view.findViewById(R.id.letters)).removeAllViews();
((FrameLayout) view.findViewById(R.id.letters)).addView(letters.getLetterLayout());

// Adapters:
((TextView) view.findViewById(R.id.item_users_letter)).setText(letters.getLetter(position));
```

## Screenshot

![Alt text](https://raw.githubusercontent.com/keygenqt/android-letters/master/screenshot/contacts.jpg "Contacts")