<template>
  <ul style="list-style-type: none;">
    <li style="color:black;font-size:20px">
      <span
        v-html="
          convertMarkDown(
            'Left Items'
          )
        "
      />
    </li>
    <li v-for="leftIcons in questionDetails.leftIcons" :key="leftIcons.id">
      <span
        v-html="
          convertMarkDown(
            leftIcons.content
          )
        "
      />
    </li>
    <li style="color:black;font-size:20px">
      <span
        v-html="
          convertMarkDown(
            'Right Items'
          )
        "
      />
    </li>
    <li v-for="rightIcons in questionDetails.rightIcons" :key="rightIcons.id">
      <span
        v-html="
          convertMarkDown(
            rightIcons.content + '\n' +
            studentAnswered(rightIcons.id) +
            ' matches with ' + rightIcons.match
          )
        "
      />
      
    </li>
  </ul>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Question from '@/models/management/Question';
import Image from '@/models/management/Image';
import ItemCombinationQuestionDetails from '@/models/management/questions/ItemCombinationQuestionDetails';
import ItemCombinationAnswerDetails from '@/models/management/questions/ItemCombinationAnswerDetails';

@Component
export default class ItemCombinationView extends Vue {
  @Prop() readonly questionDetails!: ItemCombinationQuestionDetails;
  @Prop() readonly answerDetails?: ItemCombinationAnswerDetails;

  studentAnswered(option: number) {
    //return this.answerDetails && this.answerDetails?.option.id === option
     // ? '**[S]** '
      //: '';
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>
